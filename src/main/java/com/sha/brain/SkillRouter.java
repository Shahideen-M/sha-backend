package com.sha.brain;

import com.sha.brain.prompt.SkillPrompt;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import com.sha.service.SkillRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SkillRouter {

    private final SkillRegistry registry;

    public SkillRouter(SkillRegistry registry) {
        this.registry = registry;
    }

    public List<SkillType> findPossibleSkills(String userMessage) {

        String message = userMessage.toLowerCase();
        List<SkillType> possibleSkills = new ArrayList<>();

        for (Skill<?, ?> skill : registry.getAllSkills()) {
            SkillPrompt<?> prompt = skill.describe();
            if (prompt == null || prompt.getKeywords() == null) {
                continue;
            }
            for (String keyword : prompt.getKeywords()) {
                if (message.contains(keyword.toLowerCase())) {
                    possibleSkills.add(skill.getType());
                    break;
                }
            }
        }
        if (possibleSkills.isEmpty()) {
            for (Skill<?, ?> skill : registry.getAllSkills()) {
                possibleSkills.add(skill.getType());
            }
        }

        return possibleSkills;
    }

}