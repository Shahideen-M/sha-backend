package com.sha.brain;

import com.sha.brain.prompt.SkillPrompt;
import com.sha.brain.enums.SkillType;
import com.sha.service.Skill;
import com.sha.service.SkillRegistry;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SkillRouter {

    private final SkillRegistry registry;

    public SkillRouter(SkillRegistry registry) {
        this.registry = registry;
    }

    public List<SkillType> findPossibleSkills(String userMessage) {

        String message = userMessage.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
        Set<SkillType> possibleSkills = new LinkedHashSet<>();

        for (Skill<?, ?> skill : registry.getAllSkills()) {
            SkillPrompt<?> prompt = skill.describe();
            if (prompt == null || prompt.getKeywords() == null) {
                continue;
            }
            for (String keyword : prompt.getKeywords()) {
                String normalizedKeyword = keyword.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
                if (message.contains(normalizedKeyword)) {
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

        return new ArrayList<>(possibleSkills);
    }

}