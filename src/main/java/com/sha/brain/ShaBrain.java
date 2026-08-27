package com.sha.brain;

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

    private final AIRouter aiRouter;
    private final ObjectMapper objectMapper;
    private final ShaBrainPromptBuilder promptBuilder;
    private final ShaBrainValidator validator;
    private final SkillRegistry skillRegistry;
    private final SkillRouter skillRouter;
    private final AuthorityManager authorityManager;
    private final ApprovalStore approvalStore;

    public ShaBrainResponse process(String userMessage) {

        List<SkillType> possibleSkills = skillRouter.findPossibleSkills(userMessage);

        String prompt = promptBuilder.buildPrompt(userMessage, possibleSkills);

        System.out.println(prompt);

        ChatRequest request = new ChatRequest(prompt);
        ChatResponse response = aiRouter.geminiChat(request);

        System.out.println(response.getResponse());

        try {

            ShaBrainResponse brainResponse =
                    objectMapper.readValue(
                            response.getResponse(),
                            ShaBrainResponse.class
                    );

            validator.validate(brainResponse);

            if (brainResponse.getType() != com.sha.brain.enums.ShaResponseType.SKILL_CALL) return brainResponse;

            Skill skill =
                    skillRegistry.findSkill(
                            brainResponse.getSkill(),
                            Skill.class
                    );

            ObjectNode parameters = brainResponse.getParameters() != null
                    ? (ObjectNode) brainResponse.getParameters()
                    : objectMapper.createObjectNode();

            parameters.put(
                    "operation",
                    brainResponse.getOperation()
            );

            Object req =
                    objectMapper.convertValue(
                            parameters,
                            skill.getRequestClass()
                    );

            AuthorityLevel authority = authorityManager.check(
                    brainResponse.getSkill(),
                    brainResponse.getOperation()
            );

            if (authority == AuthorityLevel.SAFE) {
                Object exe = skill.execute(req);
                JsonNode data = objectMapper.valueToTree(exe);
                brainResponse.setData(data);
                return brainResponse;
            }

            if (authority == AuthorityLevel.BLOCKED) {
                throw new RuntimeException("Action blocked by Sha authority policy.");
            }

            if (authority == AuthorityLevel.APPROVAL_REQUIRED) {
                PendingAction pendingAction = new PendingAction(
                        brainResponse.getSkill(),
                        brainResponse.getOperation(),
                        brainResponse.getParameters(),
                        authority
                );

                String token = approvalStore.create(pendingAction);

                brainResponse.setApprovalRequired(true);
                brainResponse.setApprovalToken(token);

                brainResponse.setMessage(
                        "Approval required before executing "
                                + brainResponse.getOperation()
                                + " on "
                                + brainResponse.getSkill()
                );
                return brainResponse;
            }

            Object exe = skill.execute(req);
            JsonNode data = objectMapper.valueToTree(exe);
            brainResponse.setData(data);

            return brainResponse;

        } catch (JacksonException e) {

            throw new RuntimeException("Failed to parse ShaBrain response" + e);
        }
    }
}