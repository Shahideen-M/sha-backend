package com.sha.service;

import com.sha.dto.request.*;
import com.sha.dto.response.*;
import com.sha.enums.SkillType;
import com.sha.service.impl.AIRouter;
import com.sha.service.skills.DeveloperAssistantSkill;
import com.sha.service.skills.FileSkill;
import com.sha.service.skills.ProjectReaderSkill;
import com.sha.service.skills.TradeCalculatorSkill;
import org.springframework.stereotype.Service;

@Service
public class ShaService {

    private final SkillRegistry skillRegistry;
    private final AIRouter aiRouter;

    public ShaService (SkillRegistry skillRegistry, AIRouter aiRouter) {
        this.skillRegistry = skillRegistry;
        this.aiRouter = aiRouter;
    }

    public ChatResponse chat(ChatRequest chatRequest) {
        return aiRouter.chat(chatRequest);
    }

    public TradeCalculationResponse calculateTrade(TradeCalculationRequest request) {
        TradeCalculatorSkill skill = skillRegistry.findSkill(
                SkillType.TRADE_CALCULATOR,
                TradeCalculatorSkill.class
        );
        return skill.execute(request);
    }

    public DeveloperAssistantResponse devAssistance(DeveloperAssistantRequest request) {
        DeveloperAssistantSkill skill = skillRegistry.findSkill(
                SkillType.AI,
                DeveloperAssistantSkill.class
        );
        return skill.execute(request);
    }

    public FileResponse fileOperation(FileRequest request) {
        FileSkill skill = skillRegistry.findSkill(
                SkillType.FILE,
                FileSkill.class
        );
        return skill.execute(request);
    }

    public ProjectReaderResponse projectOperation(ProjectReaderRequest request) {
        ProjectReaderSkill skill = skillRegistry.findSkill(
                SkillType.PROJECT,
                ProjectReaderSkill.class
        );
        return skill.execute(request);
    }

}
