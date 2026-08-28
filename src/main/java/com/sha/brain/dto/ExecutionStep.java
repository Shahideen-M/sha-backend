package com.sha.brain.dto;

import com.sha.enums.SkillType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ExecutionStep {

    private SkillType skill;
    private String operation;
    private JsonNode parameters;
}
