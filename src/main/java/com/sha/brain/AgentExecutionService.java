package com.sha.brain;

import com.sha.agents.developer.tools.DeveloperToolkit;
import com.sha.agents.tools.AgentShaTool;
import com.sha.brain.approval.AgentApprovalService;
import com.sha.brain.approval.AgentPendingApproval;
import com.sha.brain.approval.ApprovedAgentAction;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.enums.ShaResponseType;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentExecutionService {

    private final ChatClient chatClient;
    private final AgentApprovalService approvalService;
    private final ToolCallingManager toolCallingManager;
    private final DeveloperToolkit toolkit;
    private final ChatMemory chatMemory;

    public AgentExecutionService(
            @Qualifier("nvidiaChatClient") ChatClient chatClient,
            AgentApprovalService approvalService,
            ToolCallingManager toolCallingManager,
            DeveloperToolkit toolkit,
            ChatMemory chatMemory
    ) {
        this.chatClient = chatClient;
        this.approvalService = approvalService;
        this.toolCallingManager = toolCallingManager;
        this.toolkit = toolkit;
        this.chatMemory = chatMemory;
    }

    public ShaBrainResponse execute(
            String agentName,
            String projectPath,
            String systemPrompt,
            String userMessage
    ) {

        List<AgentShaTool> tools;

        try {
            tools = toolkit.forProject(projectPath);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }

        List<org.springframework.ai.tool.ToolCallback> callbacks =
                tools.stream()
                        .map(AgentShaTool::getTool)
                        .toList();

        ToolCallingChatOptions.Builder optionsBuilder =
                ToolCallingChatOptions.builder()
                        .toolCallbacks(callbacks);

        ToolCallingChatOptions options = optionsBuilder.build();

        var response = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId(agentName)))
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
                        new SystemMessage(systemPrompt),
                        new org.springframework.ai.chat.messages.UserMessage(
                                userMessage
                        )
                ),
                options
        );

        return driveAgentLoop(
                tools,
                optionsBuilder,
                agentName,
                projectPath,
                systemPrompt,
                userMessage,
                prompt,
                chatResponse
        );
    }

    public ShaBrainResponse resume(ApprovedAgentAction approvedAction) {

        AgentPendingApproval approval = approvedAction.approval();
        List<AgentShaTool> tools;

        try {
            tools = toolkit.forProject(approval.projectPath());
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }

        List<org.springframework.ai.tool.ToolCallback> callbacks =
                tools.stream()
                        .map(AgentShaTool::getTool)
                        .toList();

        ToolCallingChatOptions.Builder optionsBuilder =
                ToolCallingChatOptions.builder()
                        .toolCallbacks(callbacks);

        ToolCallingChatOptions options = optionsBuilder.build();

        String result;
        try {
            result = approval.tool()
                    .getTool()
                    .call(approval.toolArguments());
        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }

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
                result == null ? "" : result
        ));

        List<Message> history = new ArrayList<>();
        history.add(new SystemMessage(approval.systemPrompt()));
        history.addAll(chatMemory.get(conversationId(approval.agentName())));
        history.add(ToolResponseMessage.builder().responses(responses).build());

        ChatResponse chatResponse = callModel(history, optionsBuilder, approval.agentName());

        if (chatResponse == null) {
            return error("No response from AI.");
        }

        return driveAgentLoop(
                tools,
                optionsBuilder,
                approval.agentName(),
                approval.projectPath(),
                approval.systemPrompt(),
                approval.userMessage(),
                new Prompt(history, options),
                chatResponse
        );
    }

    private ShaBrainResponse driveAgentLoop(
            List<AgentShaTool> tools,
            ToolCallingChatOptions.Builder optionsBuilder,
            String agentName,
            String projectPath,
            String systemPrompt,
            String userMessage,
            Prompt prompt,
            ChatResponse chatResponse
    ) {

        while (chatResponse.hasToolCalls()) {

            List<AssistantMessage.ToolCall> toolCalls =
                    chatResponse.getResult()
                            .getOutput()
                            .getToolCalls();

            int gatedIndex = -1;
            for (int i = 0; i < toolCalls.size(); i++) {
                AgentShaTool tool = findTool(tools, toolCalls.get(i).name());
                if (tool == null) {
                    return error("Unknown tool: " + toolCalls.get(i).name());
                }
                if (tool.getAuthority() == AuthorityLevel.BLOCKED) {
                    return error("Action blocked by Sha authority policy.");
                }
                if (tool.getAuthority() == AuthorityLevel.APPROVAL_REQUIRED) {
                    gatedIndex = i;
                    break;
                }
            }

            if (gatedIndex >= 0) {

                List<AgentPendingApproval.AnsweredToolCall> answeredCalls =
                        answerRemainingCalls(tools, toolCalls, gatedIndex);

                AssistantMessage.ToolCall gatedCall = toolCalls.get(gatedIndex);
                AgentShaTool gatedTool = findTool(tools, gatedCall.name());

                String approvalId = approvalService.create(
                        agentName,
                        projectPath,
                        gatedCall.name(),
                        gatedCall.arguments(),
                        userMessage,
                        systemPrompt,
                        gatedCall.id(),
                        answeredCalls,
                        gatedTool
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

            ToolExecutionResult executionResult =
                    toolCallingManager.executeToolCalls(prompt, chatResponse);

            List<Message> history = executionResult.conversationHistory();

            prompt = new Prompt(history, optionsBuilder.build());

            chatResponse = callModel(history, optionsBuilder, agentName);

            if (chatResponse == null) {
                return error("No response from AI.");
            }
        }

        return new ShaBrainResponse(
                ShaResponseType.CHAT,
                chatResponse.getResult()
                        .getOutput()
                        .getText(),
                null,
                false,
                ""
        );
    }

    private List<AgentPendingApproval.AnsweredToolCall> answerRemainingCalls(
            List<AgentShaTool> tools,
            List<AssistantMessage.ToolCall> toolCalls,
            int gatedIndex
    ) {
        List<AgentPendingApproval.AnsweredToolCall> answered = new ArrayList<>();

        for (int i = 0; i < toolCalls.size(); i++) {

            if (i == gatedIndex) {
                continue;
            }

            AssistantMessage.ToolCall toolCall = toolCalls.get(i);
            AgentShaTool tool = findTool(tools, toolCall.name());

            if (tool == null) {
                answered.add(new AgentPendingApproval.AnsweredToolCall(
                        toolCall.id(),
                        toolCall.name(),
                        "Error: unknown tool."
                ));
                continue;
            }

            if (tool.getAuthority() == AuthorityLevel.BLOCKED) {
                answered.add(new AgentPendingApproval.AnsweredToolCall(
                        toolCall.id(),
                        toolCall.name(),
                        "Error: blocked by Sha authority policy."
                ));
                continue;
            }

            if (tool.getAuthority() == AuthorityLevel.APPROVAL_REQUIRED) {
                answered.add(new AgentPendingApproval.AnsweredToolCall(
                        toolCall.id(),
                        toolCall.name(),
                        "Deferred: not executed. This action also requires approval."
                ));
                continue;
            }

            String safeResult;
            try {
                safeResult = tool.getTool().call(toolCall.arguments());
            } catch (Exception e) {
                safeResult = "Error: " + e.getMessage();
            }

            answered.add(new AgentPendingApproval.AnsweredToolCall(
                    toolCall.id(),
                    toolCall.name(),
                    safeResult == null ? "" : safeResult
            ));
        }

        return answered;
    }

    private ChatResponse callModel(
            List<Message> history,
            ToolCallingChatOptions.Builder optionsBuilder,
            String agentName
    ) {

        return chatClient.prompt()
                .messages(history)
                .options(optionsBuilder)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId(agentName)))
                .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                .call()
                .chatClientResponse()
                .chatResponse();
    }

    private String conversationId(String agentName) {
        return "agent-" + agentName;
    }

    private AgentShaTool findTool(List<AgentShaTool> tools, String toolName) {

        return tools.stream()
                .filter(tool -> tool.getTool()
                        .getToolDefinition()
                        .name()
                        .equals(toolName))
                .findFirst()
                .orElse(null);
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