package com.sha.brain;

import com.sha.skills.Skill;

public record PendingApproval(
        String id,
        Skill<?, ?> skill,
        Object request
) {
}
