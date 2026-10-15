package com.sha.brain.approval;

import com.sha.agents.tools.AgentShaTool;
import org.springaicommunity.agent.tools.TodoWriteTool;

import java.util.List;

public record AgentPendingApproval(
        String id,
        String toolName,
        String toolArguments,
        String agentName,
        String projectPath,
        String userMessage,
        String systemPrompt,
        String gatedToolCallId,
        List<AnsweredToolCall> answeredCalls,
        AgentShaTool tool,
        int iterations,
        String conversationId,
        TodoWriteTool.Todos plan
) {

    public record AnsweredToolCall(
            String toolCallId,
            String name,
            String result
    ) {
    }

    public List<AnsweredToolCall> answeredCalls() {
        return answeredCalls == null ? List.of() : answeredCalls;
    }
}