package com.sha.brain.dto;

import com.sha.brain.enums.AuthorityLevel;
import com.sha.enums.SkillType;
import tools.jackson.databind.JsonNode;

public record PendingAction(
        SkillType skill,
        String operation,
        JsonNode parameters,
        AuthorityLevel authority) {
}
