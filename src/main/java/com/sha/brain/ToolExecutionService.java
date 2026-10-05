package com.sha.brain;

import com.sha.brain.approval.ApprovalService;
import com.sha.brain.approval.ApprovedAction;
import com.sha.brain.approval.PendingApproval;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.enums.ShaResponseType;
import com.sha.skills.tools.ShaTool;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
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
public class ToolExecutionService {

    private static final String SYSTEM_PROMPT = """
            You are Sha, a helpful AI assistant.

            You are the brain of Sha.
            You decide which available tools are needed.

            Use tools when they are necessary to perform
            an action requested by the user.

            You may use multiple tools in sequence.

            Never claim an action was completed unless
            the corresponding tool actually succeeded.
            """;

    private final ChatClient chatClient;
    private final List<ShaTool> tools;
    private final ApprovalService approvalService;
    private final ToolCallingManager toolCallingManager;
    private final ChatMemory chatMemory;

    public ToolExecutionService(
            @Qualifier("groqChatClient") ChatClient chatClient,
            List<ShaTool> tools,
            ApprovalService approvalService,
            ToolCallingManager toolCallingManager,
            ChatMemory chatMemory
    ) {
        this.chatClient = chatClient;
        this.tools = tools;
        this.approvalService = approvalService;
        this.toolCallingManager = toolCallingManager;
        this.chatMemory = chatMemory;
    }

    public ShaBrainResponse execute(String userMessage) {

        ToolCallingChatOptions.Builder optionsBuilder =
                ToolCallingChatOptions
                        .builder()
                        .toolCallbacks(buildCallbacks());

        var response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, "default"))
                .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                .tools(buildCallbacks())
                .call()
                .chatClientResponse();
        ChatResponse chatResponse = response.chatResponse();

        if (chatResponse == null) {
            return error("No response from AI.");
        }

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage(SYSTEM_PROMPT),
                        new org.springframework.ai.chat.messages.UserMessage(userMessage)
                ),
                optionsBuilder.build()
        );

        return driveToolLoop(
                optionsBuilder,
                userMessage,
                prompt,
                chatResponse
        );
    }

    public ShaBrainResponse resume(ApprovedAction approvedAction) {
        PendingApproval approval = approvedAction.approval();

        Object result;
        try {
            result = approval.skill().execute(approval.request());
        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }

        ToolCallingChatOptions.Builder optionsBuilder =
                ToolCallingChatOptions
                        .builder()
                        .toolCallbacks(buildCallbacks());

        List<ToolResponseMessage.ToolResponse> responses =
                new ArrayList<>(approval.answeredCalls().stream()
                        .map(call -> new ToolResponseMessage.ToolResponse(
                                call.toolCallId(),
                                call.name(),
                                call.result()
                        ))
                        .toList());

        responses.add(new ToolResponseMessage.ToolResponse(
                approval.gatedToolCallId(),
                approval.toolName(),
                String.valueOf(result)
        ));

        List<Message> history = new ArrayList<>();
        history.add(new SystemMessage(approval.systemPrompt()));
        history.addAll(chatMemory.get("default"));
        history.add(ToolResponseMessage.builder().responses(responses).build());

        ChatResponse chatResponse = callModel(history, optionsBuilder);

        if (chatResponse == null) {
            return error("No response from AI.");
        }

        return driveToolLoop(
                optionsBuilder,
                approval.userMessage(),
                new Prompt(history, optionsBuilder.build()),
                chatResponse
        );
    }

    private ShaBrainResponse driveToolLoop(
            ToolCallingChatOptions.Builder optionsBuilder,
            String userMessage,
            Prompt prompt,
            ChatResponse chatResponse
    ) {

        while (chatResponse.hasToolCalls()) {
            List<AssistantMessage.ToolCall> toolCalls = chatResponse.getResult().getOutput().getToolCalls();

            int gatedIndex = -1;
            List<ShaTool> selectedTools = new ArrayList<>();
            List<Object> requests = new ArrayList<>();

            for (int i = 0; i < toolCalls.size(); i++) {
                ShaTool selectedTool = findTool(toolCalls.get(i).name());
                if (selectedTool == null) return error("Unknown tool: " + toolCalls.get(i).name());
                Object request;
                try {
                    request = selectedTool.createRequest(toolCalls.get(i).name(), toolCalls.get(i).arguments());
                } catch (Exception e) {
                    return error(
                            "Could not create request for "
                                    + toolCalls.get(i).name()
                                    + ": "
                                    + e.getMessage()
                    );
                }
                AuthorityLevel authority = selectedTool.getSkill().getAuthority(request);
                if (authority == AuthorityLevel.BLOCKED) {
                    return new ShaBrainResponse(
                            ShaResponseType.ERROR,
                            "Action blocked by Sha authority policy.",
                            null,
                            false,
                            ""
                    );
                }
                selectedTools.add(selectedTool);
                requests.add(request);
                if (authority == AuthorityLevel.APPROVAL_REQUIRED) {
                    gatedIndex = i;
                    break;
                }
            }

            if (gatedIndex >= 0) {
                AssistantMessage.ToolCall gatedCall = toolCalls.get(gatedIndex);
                String approvalId = approvalService.create(
                        gatedCall.name(),
                        userMessage,
                        SYSTEM_PROMPT,
                        gatedCall.id(),
                        answerRemainingCalls(toolCalls, gatedIndex),
                        selectedTools.get(gatedIndex).getSkill(),
                        requests.get(gatedIndex)
                );
                return new ShaBrainResponse(
                        ShaResponseType.APPROVAL_REQUIRED,
                        "Approval required before executing "
                                + gatedCall.name() + ".",
                        null,
                        true,
                        approvalId
                );
            }

            ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            List<Message> history = executionResult.conversationHistory();
            prompt = new Prompt(history, optionsBuilder.build());
            chatResponse = callModel(history, optionsBuilder);

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

    private List<PendingApproval.AnsweredToolCall> answerRemainingCalls(
            List<AssistantMessage.ToolCall> toolCalls,
            int gatedIndex
    ) {

        List<PendingApproval.AnsweredToolCall> answered = new ArrayList<>();

        for (int i = 0; i < toolCalls.size(); i++) {
            if (i == gatedIndex) {
                continue;
            }

            AssistantMessage.ToolCall toolCall = toolCalls.get(i);
            ShaTool tool = findTool(toolCall.name());

            if (tool == null) {
                answered.add(new PendingApproval.AnsweredToolCall(
                        toolCall.id(),
                        toolCall.name(),
                        "Error: unknown tool."
                ));
                continue;
            }

            Object request;
            AuthorityLevel authority;
            try {
                request = tool.createRequest(toolCall.name(), toolCall.arguments());
                authority = tool.getSkill().getAuthority(request);
            } catch (Exception e) {
                answered.add(new PendingApproval.AnsweredToolCall(
                        toolCall.id(),
                        toolCall.name(),
                        "Error: " + e.getMessage()
                ));
                continue;
            }

            if (authority == AuthorityLevel.BLOCKED) {
                answered.add(new PendingApproval.AnsweredToolCall(
                        toolCall.id(),
                        toolCall.name(),
                        "Error: blocked by Sha authority policy."
                ));
                continue;
            }

            if (authority == AuthorityLevel.APPROVAL_REQUIRED) {
                answered.add(new PendingApproval.AnsweredToolCall(
                        toolCall.id(),
                        toolCall.name(),
                        "Deferred: not executed. This action also requires approval."
                ));
                continue;
            }

            String safeResult;
            try {
                safeResult = String.valueOf(tool.getSkill().execute(request));
            } catch (Exception e) {
                safeResult = "Error: " + e.getMessage();
            }

            answered.add(new PendingApproval.AnsweredToolCall(
                    toolCall.id(),
                    toolCall.name(),
                    safeResult == null ? "" : safeResult
            ));
        }

        return answered;
    }

    private ChatResponse callModel(
            List<Message> history,
            ToolCallingChatOptions.Builder optionsBuilder
    ) {

        return chatClient.prompt()
                .messages(history)
                .options(optionsBuilder)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, CONVERSATION_ID))
                .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                .call()
                .chatClientResponse()
                .chatResponse();
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