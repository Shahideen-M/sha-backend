package com.sha.brain;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import com.sha.service.SkillRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShaBrainPromptBuilder {

    private final SkillRegistry skillRegistry;

    public ShaBrainPromptBuilder(SkillRegistry skillRegistry) {
        this.skillRegistry = skillRegistry;
    }

    public String buildIdentity() {

        return """
                You are ShaBrain.
                
                You are a routing engine, not an assistant.
                
                Your only job is to determine whether a backend skill should be executed.
                
                Never pretend that an action has been performed.
                
                If an available skill can satisfy the user's request, always return SKILL_CALL.
            """;
    }

    public String buildRules() {

        return """
            Rules:
            
            - Return ONLY valid JSON.
            - The first character must be {.
            - The last character must be }.
            - Never return Markdown.
            - Never use ```json or ``` in the response.
            - Never return explanations or text outside the JSON.
            - Never describe an action that requires a backend skill.
            - When returning follow this operation + parameters.
            - If an available skill can perform the request, return SKILL_CALL.
            - CHAT responses are only for normal conversation or questions.
            - CLARIFICATION is only when the request is ambiguous or required information is missing.
            - ERROR is only when no available skill can perform the request.
        """;
    }

    public String buildSkills(List<SkillType> possibleSkills) {

        StringBuilder sb = new StringBuilder();

        sb.append("Available Skills:\n\n");

        for (Skill<?, ?> skill : skillRegistry.getAllSkills()) {

            if (!possibleSkills.contains(skill.getType())) continue;

            SkillPrompt<?> prompt = skill.describe();

            if (prompt == null) {
                continue;
            }

            sb.append("Skill: ").append(prompt.getSkill()).append("\n");
            sb.append("Purpose: ").append(prompt.getPurpose()).append("\n");

            for (OperationPrompt<?> operation : prompt.getOperations()) {

                sb.append("\nOperation: ")
                        .append(operation.getOperation())
                        .append("\n");

                sb.append("Description: ")
                        .append(operation.getDescription())
                        .append("\n");

                sb.append("Parameters: ")
                        .append(operation.getParameters())
                        .append("\n");

                sb.append("Example:\n")
                        .append(operation.getExampleJson())
                        .append("\n");
            }
            sb.append("\n---------------------------------\n\n");
        }
        return sb.toString();
    }

    public String buildUserMessage(String message) {

        return """
            User Request:
                       \s
            %s
           \s""".formatted(message);
    }

    public String buildResponseFormat() {
        return """
        Response Format

        CHAT

        {
          "type":"CHAT",
          "message":"..."
        }

        SKILL_CALL

        {
          "type":"SKILL_CALL",
          "skill":"FILE",
          "operation":"READ",
          "parameters":{
            ...
          }
        }

        CLARIFICATION

        {
          "type":"CLARIFICATION",
          "message":"..."
        }

        ERROR

        {
          "type":"ERROR",
          "message":"..."
        }
        """;
    }

    public String buildPrompt(String message, List<SkillType> possibleSkills) {

        return buildIdentity()
                + "\n\n"
                + buildRules()
                + "\n\n"
                + buildSkills(possibleSkills)
                + "\n\n"
                + buildResponseFormat()
                + "\n\n"
                + buildUserMessage(message);
    }
}
