package com.sha.brain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public ShaBrainResponse process(String userMessage) {

        List<SkillType> possibleSkills =
                skillRouter.findPossibleSkills(userMessage);

        String prompt =
                promptBuilder.buildPrompt(userMessage, possibleSkills);

        System.out.println(prompt);

        ChatRequest request = new ChatRequest(prompt);
        ChatResponse response = aiRouter.chat(request);

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

            var parameters =
                    (com.fasterxml.jackson.databind.node.ObjectNode)
                            brainResponse.getParameters();

            parameters.put(
                    "operation",
                    brainResponse.getOperation()
            );

            Object req =
                    objectMapper.convertValue(
                            parameters,
                            skill.getRequestClass()
                    );

            Object exe = skill.execute(req);
            brainResponse.setMessage(exe.toString());

            return brainResponse;

        } catch (JsonProcessingException e) {

            throw new RuntimeException("ERROR" + e);
        }
    }
}