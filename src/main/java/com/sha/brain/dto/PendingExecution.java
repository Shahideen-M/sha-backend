package com.sha.brain.dto;

import java.util.List;

public record PendingExecution (
        ExecutionPlan plan,
        int currentStepIndex,
        List<StepResult> stepResults
) {
}
