package com.sha.service;

import com.sha.dto.ChatRequest;
import com.sha.dto.ChatResponse;
import com.sha.dto.TradeCalculationRequest;
import com.sha.dto.TradeCalculationResponse;
import com.sha.enums.SkillType;
import com.sha.service.skills.TradeCalculatorSkill;
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

    public TradeCalculationResponse calculateTrade(TradeCalculationRequest request) {
        TradeCalculatorSkill skill = skillRegistry.findSkill(
                SkillType.TRADE_CALCULATOR,
                TradeCalculatorSkill.class);
        return skill.execute(request);
    }

}
