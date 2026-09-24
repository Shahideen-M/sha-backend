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
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentExecutionService {

    private final ChatClient chatClient;
    private final AgentApprovalService approvalService;
    private final ToolCallingManager toolCallingManager;
    private final DeveloperToolkit toolkit;

    public AgentExecutionService(
            @Qualifier("geminiChatClient") ChatClient chatClient,
            AgentApprovalService approvalService,
            ToolCallingManager toolCallingManager,
            DeveloperToolkit toolkit
    ) {
        this.chatClient = chatClient;
        this.approvalService = approvalService;
        this.toolCallingManager = toolCallingManager;
        this.toolkit = toolkit;
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
                        new org.springframework.ai.chat.messages.SystemMessage(
                                systemPrompt
                        ),
                        new org.springframework.ai.chat.messages.UserMessage(
                                userMessage
                        )
                ),
                options
        );

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

                StringBuilder safeResults = new StringBuilder();
                for (int i = 0; i < gatedIndex; i++) {
                    AssistantMessage.ToolCall safeCall = toolCalls.get(i);
                    AgentShaTool safeTool = findTool(tools, safeCall.name());
                    String safeResult;
                    try {
                        safeResult = safeTool.getTool().call(safeCall.arguments());
                    } catch (Exception e) {
                        safeResult = "Error: " + e.getMessage();
                    }
                    safeResults.append(safeCall.name())
                            .append(": ")
                            .append(safeResult)
                            .append(System.lineSeparator());
                }

                AssistantMessage.ToolCall gatedCall = toolCalls.get(gatedIndex);
                AgentShaTool gatedTool = findTool(tools, gatedCall.name());

                String approvalId = approvalService.create(
                        agentName,
                        projectPath,
                        gatedCall.name(),
                        gatedCall.arguments(),
                        userMessage,
                        systemPrompt,
                        safeResults.toString(),
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
                chatResponse.getResult()
                        .getOutput()
                        .getText(),
                null,
                false,
                ""
        );
    }

    public ShaBrainResponse resume(ApprovedAgentAction approvedAction) {

        AgentPendingApproval approval = approvedAction.approval();

        String result;
        try {
            result = approval.tool()
                    .getTool()
                    .call(approval.toolArguments());
        } catch (Exception e) {
            return error("Approved action failed: " + e.getMessage());
        }

        String continuation = """
                Continue the user's original task.

                Original request:
                %s

                Approved tool:
                %s

                Tool result:
                %s

                %s

                Continue from this point.
                Do not repeat the completed action.
                """
                .formatted(
                        approval.userMessage(),
                        approval.toolName(),
                        result,
                        approval.safeResults() == null
                                || approval.safeResults().isBlank()
                                ? ""
                                : "Read-only results gathered before approval:\n"
                                        + approval.safeResults()
                );
        return execute(
                approval.agentName(),
                approval.projectPath(),
                approval.systemPrompt(),
                continuation
        );
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