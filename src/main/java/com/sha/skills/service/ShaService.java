package com.sha.skills.service;

import com.sha.agents.careeragent.CareerAgent;
import com.sha.agents.careeragent.dto.CareerRequest;
import com.sha.agents.careeragent.dto.CareerResponse;
import com.sha.agents.contentcreator.ContentCreatorAgent;
import com.sha.agents.contentcreator.VideoEditingAgent;
import com.sha.agents.contentcreator.dto.VideoEditorRequest;
import com.sha.agents.contentcreator.dto.VideoEditorResponse;
import com.sha.agents.enums.AgentType;
import com.sha.agents.service.AgentRegistry;
import com.sha.skills.enums.SkillType;
import com.sha.agents.contentcreator.dto.ContentCreatorRequest;
import com.sha.agents.contentcreator.dto.ContentCreatorResponse;

import com.sha.skills.*;
import com.sha.skills.dto.request.*;
import com.sha.skills.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShaService {

    private final SkillRegistry skillRegistry;
    private final AgentRegistry agentRegistry;

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

    public AppLauncherResponse appLauncher(AppLauncherRequest request) {
        AppLauncherSkill skill = skillRegistry.findSkill(
                SkillType.APP,
                AppLauncherSkill.class
        );
        return skill.execute(request);
    }

    public BrowserResponse browser(BrowserRequest request) {
        BrowserSkill skill = skillRegistry.findSkill(
                SkillType.BROWSER,
                BrowserSkill.class
        );
        return skill.execute(request);
    }

    public MemoryResponse memory(MemoryRequest request) {
        MemorySkill skill = skillRegistry.findSkill(
                SkillType.MEMORY,
                MemorySkill.class
        );
        return skill.execute(request);
    }

    public IslamicResponse islam(IslamicRequest request) {
        IslamicSkill skill = skillRegistry.findSkill(
                SkillType.ISLAM,
                IslamicSkill.class
        );
        return skill.execute(request);
    }

    public ContentCreatorResponse content(ContentCreatorRequest request) {
        ContentCreatorAgent agent = agentRegistry.findAgent(
                AgentType.CONTENT_CREATOR,
                ContentCreatorAgent.class
        );
        return agent.execute(request);
    }

    public VideoEditorResponse videoEditor(VideoEditorRequest request) {
        VideoEditingAgent agent = agentRegistry.findAgent(
                AgentType.VIDEO_EDITING,
                VideoEditingAgent.class
        );
        return agent.execute(request);
    }

    public CareerResponse career(CareerRequest request) {
        CareerAgent agent = agentRegistry.findAgent(
                AgentType.CAREER,
                CareerAgent.class
        );
        return agent.execute(request);
    }
}
