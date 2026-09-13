package com.sha.brain.approval;

import com.sha.skills.Skill;

public record PendingApproval(
        String id,
        String toolName,
        String userMessage,
        Skill<?, ?> skill,
        Object request
) {
}
