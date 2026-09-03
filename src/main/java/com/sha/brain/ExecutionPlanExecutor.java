package com.sha.brain;

import com.sha.agentsData.enums.AgentType;
import com.sha.agentsData.service.Agent;
import com.sha.agentsData.service.AgentRegistry;
import com.sha.brain.dto.*;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.enums.ExecutionTargetType;
import com.sha.brain.enums.SkillType;
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
    private final AgentRegistry agentRegistry;
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

            Object executable;
            Class<?> requestClass;

            if (step.getType() == ExecutionTargetType.SKILL) {

                Skill skill = skillRegistry.findSkill(
                        SkillType.valueOf(step.getTarget()),
                        Skill.class
                );

                executable = skill;
                requestClass = skill.getRequestClass();

            } else if (step.getType() == ExecutionTargetType.AGENT) {

                Agent agent = agentRegistry.findAgent(
                        AgentType.valueOf(step.getTarget()),
                        Agent.class
                );

                executable = agent;
                requestClass = agent.getRequestClass();

            } else {
                throw new RuntimeException("Unknown execution target type: " + step.getType());
            }

            ObjectNode parameters = step.getParameters() != null
                    ? (ObjectNode) step.getParameters()
                    : objectMapper.createObjectNode();

            parameters.put("operation", step.getOperation());

            Object req = objectMapper.convertValue(
                    parameters,
                    requestClass
            );

            AuthorityLevel authority = authorityManager.check(
                    step.getType(),
                    step.getTarget(),
                    step.getOperation());

            if (authority == AuthorityLevel.SAFE
                    || (authority == AuthorityLevel.APPROVAL_REQUIRED && approved)) {

                try {
                    Object result;

                    if (executable instanceof  Skill skill) {
                        result = skill.execute(req);
                    } else if (executable instanceof Agent agent) {
                        result = agent.execute(req);
                    } else {
                        throw new RuntimeException("Invalid executable.");
                    }

                    JsonNode resultJson = objectMapper.valueToTree(result);
                    boolean success = resultJson.get("success").asBoolean();
                    String message = resultJson.get("message").asString();

                    stepResults.add(
                            new StepResult(
                                    step.getType(),
                                    step.getTarget(),
                                    step.getOperation(),
                                    success,
                                    message,
                                    resultJson
                            )
                    );
                } catch (Exception e) {
                    stepResults.add(
                            new StepResult(
                                    step.getType(),
                                    step.getTarget(),
                                    step.getOperation(),
                                    false,
                                    e.getMessage(),
                                    null
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
                                step.getType(),
                                step.getTarget(),
                                step.getOperation(),
                                false,
                                "This operation is blocked for your safety.",
                                null
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