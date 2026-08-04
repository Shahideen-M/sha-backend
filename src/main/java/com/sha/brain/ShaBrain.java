package com.sha.brain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.service.Skill;
import com.sha.service.SkillRegistry;
import com.sha.service.impl.AIRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShaBrain {

    private final AIRouter aiRouter;
    private final ObjectMapper objectMapper;
    private final ShaBrainPromptBuilder promptBuilder;
    private final ShaBrainValidator validator;
    private final SkillRegistry skillRegistry;

    public ShaBrainResponse process(String userMessage) {
        String prompt = promptBuilder.buildPrompt(userMessage);
        ChatRequest request = new ChatRequest(prompt);
        ChatResponse response = aiRouter.chat(request);
        System.out.println(response.getResponse());
        try {
            ShaBrainResponse brainResponse = objectMapper.readValue(response.getResponse(), ShaBrainResponse.class);
            System.out.println(brainResponse.getParameters().toPrettyString());
            validator.validate(brainResponse);
            Skill<?, ?> skill =
                    skillRegistry.findSkill(brainResponse.getSkill(), Skill.class);
            skill.getRequestClass();
            Object req = objectMapper.convertValue(brainResponse.getParameters(), skill.getRequestClass());
            Object result = skill.execute(req);
            return brainResponse;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("ERROR"+ e);
        }
    }

}