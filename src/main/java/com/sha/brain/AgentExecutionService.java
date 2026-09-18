package com.sha.brain;

import com.sha.brain.approval.ApprovalService;
import com.sha.brain.approval.ApprovedAction;
import com.sha.brain.approval.PendingApproval;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.enums.ShaResponseType;
import com.sha.skills.Skill;
import com.sha.skills.tools.ShaTool;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AgentExecutionService {

    private final ChatClient chatClient;
    private final List<ShaTool> tools;
    private final ApprovalService approvalService;
    private final ToolCallingManager toolCallingManager;

    public AgentExecutionService(
            @Qualifier("geminiChatClient") ChatClient chatClient,
            List<ShaTool> tools,
            ApprovalService approvalService,
            ToolCallingManager toolCallingManager
    ) {
        this.chatClient = chatClient;
        this.tools = tools;
        this.approvalService = approvalService;
        this.toolCallingManager = toolCallingManager;
    }

    public ShaBrainResponse execute(
            String agentName,
            String systemPrompt,
            String userMessage
    ) {

        List<ToolCallback> callbacks = buildCallbacks();

        ToolCallingChatOptions.Builder optionsBuilder =
                ToolCallingChatOptions
                        .builder()
                        .toolCallbacks(callbacks);

        ToolCallingChatOptions options = optionsBuilder.build();
        var response = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, "agent-" + agentName))
                .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                .tools(callbacks)
                .call()
                .chatClientResponse();
        ChatResponse chatResponse = response.chatResponse();

        if (chatResponse == null) {
            return error("No response from AI.");
        }

        Prompt prompt = new Prompt(
                List.of(
                        new org.springframework.ai.chat.messages.SystemMessage(systemPrompt),
                        new org.springframework.ai.chat.messages.UserMessage(userMessage)
                ), options
        );

        while (chatResponse.hasToolCalls()) {
            for (var toolCall : chatResponse.getResult().getOutput().getToolCalls()) {
                ShaTool selectedTool = findTool(toolCall.name());
                if (selectedTool == null) return error("Unknown tool: " + toolCall.name());
                Skill<?, ?> skill = selectedTool.getSkill();
                Object request;
                try {
                    request = selectedTool.createRequest(toolCall.name(), toolCall.arguments());
                } catch (Exception e) {
                    return error(
                            "Could not create request for "
                                    + toolCall.name()
                                    + ": "
                                    + e.getMessage()
                    );
                }

                AuthorityLevel authority = skill.getAuthority(request);
                if (authority == AuthorityLevel.BLOCKED) {
                    return new ShaBrainResponse(
                            ShaResponseType.ERROR,
                            "Action blocked by Sha authority policy.",
                            null,
                            false,
                            ""
                    );
                }
                if (authority == AuthorityLevel.APPROVAL_REQUIRED) {
                    String approvalId = approvalService.create(toolCall.name(), userMessage, skill, request);
                    return new ShaBrainResponse(
                            ShaResponseType.APPROVAL_REQUIRED,
                            "Approval required before executing "
                                    + toolCall.name() + ".",
                            null,
                            true,
                            approvalId
                    );
                }
            }
            ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);

            List<Message> history = executionResult.conversationHistory();
            prompt = new Prompt(history, options);
            response = chatClient.prompt()
                    .messages(history)
                    .options(optionsBuilder)
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, "agent-" + agentName))
                    .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                    .call()
                    .chatClientResponse();
            chatResponse = response.chatResponse();
            if (chatResponse == null) {
                return error("No response from AI.");
            }
        }
        return new ShaBrainResponse(
                ShaResponseType.CHAT,
                chatResponse
                        .getResult()
                        .getOutput()
                        .getText(),
                null,
                false,
                ""
        );
    }

    public ShaBrainResponse resume(ApprovedAction approvedAction) {
        PendingApproval approval = approvedAction.approval();
        Object result;
        try {
            result = approval.skill().execute(approval.request());
        } catch (Exception e) {
            return error(
                    "Approved action failed: "
                            + e.getMessage()
            );
        }
        String continuation = """
                Continue the user's original task.

                Original user request:
                %s

                The following action was approved by the user
                and has now been successfully executed.

                Tool:
                %s

                Result:
                %s

                Continue the task from this point.
                Do not repeat the already completed action
                unless genuinely necessary.
                Use another tool if required.
                If the task is complete, provide the final answer.
                """
                .formatted(
                        approval.userMessage(),
                        approval.toolName(),
                        String.valueOf(result)
                );
        return execute(
                "developer",
                """
                You are Sha's Developer Agent.

                Continue the development task using the available
                development tools.

                Never claim an action succeeded unless the
                corresponding tool actually succeeded.
                """,
                continuation
        );
    }

    private List<ToolCallback> buildCallbacks() {
        List<ToolCallback> callbacks = new ArrayList<>();
        for (ShaTool tool : tools) {
            callbacks.addAll(Arrays.asList(ToolCallbacks.from(tool)));
        }
        return callbacks;
    }

    private ShaTool findTool(String toolName) {
        for (ShaTool tool : tools) {
            for (ToolCallback callback : ToolCallbacks.from(tool)) {
                if (callback.getToolDefinition().name().equals(toolName)) {
                    return tool;
                }
            }
        }
        return null;
    }

    private ShaBrainResponse error(String message) {
        return new ShaBrainResponse(
                ShaResponseType.ERROR,
                message,
                null,
                false,
                ""
        );
    }
}