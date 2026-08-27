package com.sha.brain;

import com.sha.brain.dto.PendingAction;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.service.Skill;
import com.sha.service.SkillRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalStore approvalStore;
    private final SkillRegistry skillRegistry;
    private final AuthorityManager authorityManager;
    private final ObjectMapper objectMapper;

    public Object approve(String token) {

        PendingAction pendingAction = approvalStore.consume(token);

        if (pendingAction == null) throw new RuntimeException("Invalid or expired approval token.");

        AuthorityLevel authority = authorityManager.check(
                pendingAction.skill(),
                pendingAction.operation()
        );

        if (authority != AuthorityLevel.APPROVAL_REQUIRED) {
            throw new RuntimeException("This action is not valid for approval.");
        }

        Skill skill = skillRegistry.findSkill(
                pendingAction.skill(),
                Skill.class
        );

        Object request = objectMapper.convertValue(
                pendingAction.parameters(),
                skill.getRequestClass()
        );

        return skill.execute(request);
    }

    public void reject(String token) {
        approvalStore.remove(token);
    }
}
