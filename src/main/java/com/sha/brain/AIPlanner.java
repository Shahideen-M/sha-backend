package com.sha.brain;

import com.sha.brain.dto.ExecutionPlan;
import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.brain.enums.SkillType;
import com.sha.service.impl.AIRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIPlanner implements Planner{

    private final AIRouter aiRouter;
    private final ObjectMapper objectMapper;
    private final ShaBrainPromptBuilder promptBuilder;

    @Override
    public ExecutionPlan createPlan(String userMessage, List<SkillType> possibleSkills) {

        String prompt = """
        You are Sha's execution planner.

        Convert the user's request into an execution plan.

        Use ONLY the exact target names, operation names,
        parameter names and structures provided below.
        
        A target can be either a SKILL or an AGENT.
        
        Do not invent target types, targets, operations or parameters.

        Return ONLY valid JSON.

        The first character must be {.
        The last character must be }.
        Never return Markdown.

        Execution Plan Format:
        Example 1:
        {
          "steps": [
            {
              "type": "SKILL",
              "target": "ISLAM",
              "operation": "OPERATION_NAME",
              "parameters": {}
            }
          ]
        }
        Example 2:
        {
          "steps": [
            {
              "type": "AGENT",
              "target": "CONTENT_CREATOR",
              "operation": "OPERATION_NAME",
              "parameters": {}
            }
          ]
        }
        Rules:
        - For a Skill, use "type": "SKILL".
        - For an Agent, use "type": "AGENT".
        
        """ + promptBuilder.buildSkills(possibleSkills)
                + "\n\n"
                +promptBuilder.buildAgents()
                + "\n\n"
                + promptBuilder.buildUserMessage(userMessage);

        ChatRequest request = new ChatRequest(prompt);
        ChatResponse response = aiRouter.geminiChat(request);
        String json = response.getResponse().trim();

        System.out.println(response.getResponse());

        if (json.startsWith("```")) {
            json = json
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }
        System.out.println(json);
        try {
            return objectMapper.readValue(
                    json,
                    ExecutionPlan.class
            );
        } catch (JacksonException e) {
            return new ExecutionPlan(List.of());
        }
    }
}
