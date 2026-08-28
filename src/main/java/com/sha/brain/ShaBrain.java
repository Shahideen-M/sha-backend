package com.sha.brain;

import com.sha.brain.dto.ExecutionPlan;
import com.sha.brain.dto.ExecutionResult;
import com.sha.brain.dto.PendingAction;
import com.sha.brain.enums.AuthorityLevel;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import com.sha.service.SkillRegistry;
import com.sha.service.impl.AIRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShaBrain {

    private final SkillRouter skillRouter;
    private final Planner planner;
    private final ExecutionPlanExecutor executor;

    public ExecutionResult process(String userMessage) {

        List<SkillType> possibleSkills = skillRouter.findPossibleSkills(userMessage);

        ExecutionPlan plan = planner.createPlan(userMessage, possibleSkills);
        System.out.println(plan);
        ExecutionResult result = executor.execute(plan);
        System.out.println(result);

        return result;
    }
}