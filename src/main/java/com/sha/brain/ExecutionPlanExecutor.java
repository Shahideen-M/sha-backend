package com.sha.brain;

import com.sha.brain.dto.ExecutionPlan;
import com.sha.brain.dto.ExecutionResult;
import com.sha.brain.dto.ExecutionStep;
import com.sha.brain.dto.StepResult;
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
        for (ExecutionStep step : plan.getSteps()) {
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

            if (authority == AuthorityLevel.SAFE) {

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
        }
        boolean overallSuccess = stepResults.stream()
                .allMatch(StepResult::isSuccess);

        return new ExecutionResult(overallSuccess, stepResults);
    }
}