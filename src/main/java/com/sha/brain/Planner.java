package com.sha.brain;

import com.sha.brain.dto.ExecutionPlan;
import com.sha.enums.SkillType;

import java.util.List;

public interface Planner {

    ExecutionPlan createPlan(String userMessage, List<SkillType> possibleSkills);

}
