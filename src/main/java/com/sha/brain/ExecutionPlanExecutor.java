package com.sha.brain;

import com.sha.brain.dto.*;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.service.Skill;
import com.sha.service.SkillRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExecutionPlanExecutor {

    private final SkillRegistry skillRegistry;
    private final AuthorityManager authorityManager;
    private final ApprovalStore approvalStore;
    private final ObjectMapper objectMapper;

    public ExecutionResult execute(ExecutionPlan plan) {

        List<StepResult> stepResults = new ArrayList<>();
        return executeFrom(plan, 0, stepResults, false);
    }

    private ExecutionResult executeFrom(
            ExecutionPlan plan,
            int startIndex,
            List<StepResult> stepResults,
            boolean approved
    ) {
        for (int i = startIndex; i < plan.getSteps().size(); i++) {
            ExecutionStep step = plan.getSteps().get(i);

            Skill skill = skillRegistry.findSkill(
                    step.getSkill(),
                    Skill.class
            );

            ObjectNode parameters = step.getParameters() != null
                    ? (ObjectNode) step.getParameters()
                    : objectMapper.createObjectNode();

            parameters.put("operation", step.getOperation());

            Object req = objectMapper.convertValue(
                    parameters,
                    skill.getRequestClass()
            );

            AuthorityLevel authority = authorityManager.check(step.getSkill(), step.getOperation());

            if (authority == AuthorityLevel.SAFE
                    || (authority == AuthorityLevel.APPROVAL_REQUIRED && approved)) {

                try {
                    Object result = skill.execute(req);
                    JsonNode resultJson = objectMapper.valueToTree(result);
                    boolean success = resultJson.get("success").asBoolean();
                    String message = resultJson.get("message").asString();

                    stepResults.add(
                            new StepResult(
                                    step.getSkill(),
                                    step.getOperation(),
                                    success,
                                    message
                            )
                    );
                } catch (Exception e) {
                    stepResults.add(
                            new StepResult(
                                    step.getSkill(),
                                    step.getOperation(),
                                    false,
                                    e.getMessage()
                            )
                    );
                }
            }

            if (authority == AuthorityLevel.APPROVAL_REQUIRED && !approved) {
                PendingExecution pendingExecution = new PendingExecution(plan, i, stepResults);
                String token = approvalStore.create(pendingExecution);
                return new ExecutionResult(
                        false,
                        stepResults,
                        true,
                        token
                );
            }

            if (authority == AuthorityLevel.BLOCKED) {
                stepResults.add(
                        new StepResult(
                                step.getSkill(),
                                step.getOperation(),
                                false,
                                "This operation is blocked for your safety."
                        )
                );

                return new ExecutionResult(
                        false,
                        stepResults,
                        false,
                        null
                );
            }
        }
        boolean overallSuccess = stepResults.stream()
                .allMatch(StepResult::isSuccess);

        return new ExecutionResult(overallSuccess, stepResults, false, null);
    }

    public ExecutionResult resume(PendingExecution pendingExecution) {

        return executeFrom(
                pendingExecution.plan(),
                pendingExecution.currentStepIndex(),
                pendingExecution.stepResults(),
                true
        );
    }

}