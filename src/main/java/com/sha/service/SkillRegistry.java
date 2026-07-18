package com.sha.service;

import com.sha.enums.SkillType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillRegistry {

    private List<Skill<?, ?>> skills;

    public SkillRegistry(List<Skill<?, ?>> skills) {
        this.skills = skills;
    }

    public <T extends Skill<?, ?>> T findSkill(SkillType type, Class<T> skillClass) {

        Skill<?,?> skill = skills.stream()
                .filter(s -> s.getType() == type)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        return skillClass.cast(skill);
    }
}
