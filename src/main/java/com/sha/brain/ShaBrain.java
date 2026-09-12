package com.sha.brain;

import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.enums.ShaResponseType;
import com.sha.skills.Skill;
import com.sha.skills.tools.ShaTool;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ShaBrain {

    private final ChatClient chatClient;
    private final List<ShaTool> tools;
    private final ApprovalService approvalService;

    public ShaBrain(@Qualifier("geminiChatClient") ChatClient chatClient,
                    List<ShaTool> tools,
                    ApprovalService approvalService) {
        this.chatClient = chatClient;
        this.tools = tools;
        this.approvalService = approvalService;
    }

    public ShaBrainResponse process(String userMessage) {

        List<ToolCallback> callbacks = new ArrayList<>();

        for (ShaTool tool : tools) {
            callbacks.addAll(Arrays.asList(ToolCallbacks.from(tool)));
        }

        var response = chatClient.prompt()
                .system("You are Sha, a helpful AI assistant.")
                .user(userMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, "default"))
                .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                .tools(callbacks)
                .call()
                .chatClientResponse();

        var chatResponse = response.chatResponse();

        if (chatResponse == null) {
            return new ShaBrainResponse(
                    ShaResponseType.EXECUTION_RESULT,
                    "No response from AI.",
                    null,
                    false,
                    ""
            );
        }
        if (!chatResponse.hasToolCalls()) {
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
        
        var toolCall = chatResponse.
                getResult()
                .getOutput()
                .getToolCalls()
                .getFirst();

        ShaTool selectedTool = findTool(toolCall.name());
        if (selectedTool == null) {
            return new ShaBrainResponse(
                    ShaResponseType.ERROR,
                    "Unknown tool: "+ toolCall.name(),
                    null,
                    false,
                    ""
            );
        }

        Skill<?, ?> skill = selectedTool.getSkill();

        Object request;
        try {
            request = selectedTool.createRequest(toolCall.name(), toolCall.arguments());
        } catch (Exception e) {
            return new ShaBrainResponse(
                    ShaResponseType.ERROR,
                    "Could not create request: " + e.getMessage(),
                    null,
                    false,
                    ""
            );
        }

        AuthorityLevel authority = skill.getAuthority(request);
        return switch (authority) {
            case SAFE -> {
                Object result = skill.execute(request);
                yield new ShaBrainResponse(
                        ShaResponseType.EXECUTION_RESULT,
                        result.toString(),
                        null,
                        false,
                        ""
                );
            }
            case APPROVAL_REQUIRED -> {
                String approvalId = approvalService.create(skill, request);
                yield new ShaBrainResponse(
                        ShaResponseType.EXECUTION_RESULT,
                        "Approval required.",
                        null,
                        true,
                        approvalId
                );
            }
            case BLOCKED -> new ShaBrainResponse(
                    ShaResponseType.ERROR,
                    "Action blocked.",
                    null,
                    false,
                    ""
            );
        };
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
}