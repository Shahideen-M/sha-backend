package com.sha.brain;

import com.sha.agentsData.service.Agent;
import com.sha.agentsData.service.AgentRegistry;
import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.AgentPrompt;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.brain.enums.SkillType;
import com.sha.service.Skill;
import com.sha.service.SkillRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShaBrainPromptBuilder {

    private final SkillRegistry skillRegistry;
    private final AgentRegistry agentRegistry;

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

    public String buildAgents() {

        StringBuilder sb = new StringBuilder();

        sb.append("Available Agents:\n\n");

        for (Agent<?, ?> agent : agentRegistry.getAllAgents()) {

            AgentPrompt<?> prompt = agent.describe();

            if (prompt == null) {
                continue;
            }

            sb.append("Agent: ")
                    .append(prompt.getAgent())
                    .append("\n");

            sb.append("Purpose: ")
                    .append(prompt.getPurpose())
                    .append("\n");

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

}
