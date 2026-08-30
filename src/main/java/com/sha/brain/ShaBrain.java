package com.sha.brain;

import com.sha.brain.dto.ExecutionPlan;
import com.sha.brain.dto.ExecutionResult;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.ShaResponseType;
import com.sha.enums.SkillType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShaBrain {

    private final SkillRouter skillRouter;
    private final Planner planner;
    private final ExecutionPlanExecutor executor;
    private final ObjectMapper objectMapper;

    public ShaBrainResponse process(String userMessage) {

        List<SkillType> possibleSkills = skillRouter.findPossibleSkills(userMessage);

        ExecutionPlan plan = planner.createPlan(userMessage, possibleSkills);
        System.out.println(plan);
        ExecutionResult result = executor.execute(plan);
        System.out.println(result);

        if (result.isApprovalRequired()) {
            return new ShaBrainResponse(
                    ShaResponseType.APPROVAL_REQUIRED,
                    "An action requires your approval before execution can continue.",
                    null,
                    null,
                    null,
                    null,
                    true,
                    result.getApprovalToken()
            );
        }

        if (!result.isSuccess()) {
            return new ShaBrainResponse(
                    ShaResponseType.ERROR,
                    "Execution failed",
                    null,
                    null,
                    null,
                    objectMapper.valueToTree(result),
                    false,
                    ""
            );
        }

        return new ShaBrainResponse(
                ShaResponseType.EXECUTION_RESULT,
                "Execution completed.",
                null,
                null,
                null,
                objectMapper.valueToTree(result),
                false,
                ""
        );
    }
}