package com.sha.brain.approval;

import com.sha.skills.Skill;

import java.util.List;

public record PendingApproval(
        String id,
        String toolName,
        String userMessage,
        String systemPrompt,
        String gatedToolCallId,
        List<AnsweredToolCall> answeredCalls,
        Skill<?, ?> skill,
        Object request
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