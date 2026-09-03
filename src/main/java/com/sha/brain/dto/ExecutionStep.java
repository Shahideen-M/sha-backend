package com.sha.brain.dto;

import com.sha.brain.enums.ExecutionTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ExecutionStep {

    private ExecutionTargetType type;
    private String target;
    private String operation;
    private JsonNode parameters;
}
