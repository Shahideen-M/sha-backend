package com.sha.brain.dto;

import com.sha.brain.enums.ExecutionTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StepResult {

    private ExecutionTargetType type;
    private String target;
    private String operation;
    private boolean success;
    private String message;
    private JsonNode data;
}
