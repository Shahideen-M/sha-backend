package com.sha.service;

import com.sha.dto.ChatRequest;
import com.sha.dto.ChatResponse;
import com.sha.dto.CraftCalculationRequest;
import com.sha.dto.CraftCalculationResponse;
import com.sha.enums.SkillType;
import com.sha.service.skills.CraftCalculatorSkill;
import org.springframework.stereotype.Service;

@Service
public class ShaService {

    private final SkillRegistry skillRegistry;
    private final AIService aiService;

    public ShaService (SkillRegistry skillRegistry, AIService aiService) {
        this.skillRegistry = skillRegistry;
        this.aiService = aiService;
    }

    public ChatResponse chat(ChatRequest chatRequest) {
        return aiService.chat(chatRequest);
    }

    public CraftCalculationResponse calculateCraft(CraftCalculationRequest request) {
        CraftCalculatorSkill skill = skillRegistry.findSkill(
                SkillType.CRAFT_CALCULATOR,
                CraftCalculatorSkill.class);
        return skill.execute(request);
    }

}
