package com.sha.brain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionResult {

    private boolean success;
    private List<StepResult> stepResults;
}
