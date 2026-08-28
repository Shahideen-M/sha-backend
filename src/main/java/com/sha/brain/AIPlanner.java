package com.sha.brain;

import com.sha.brain.dto.ExecutionPlan;
import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.enums.SkillType;
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

        Use ONLY the exact skill names, operation names,
        parameter names and structures provided below.

        Do not invent skills, operations or parameters.

        Return ONLY valid JSON.

        The first character must be {.
        The last character must be }.
        Never return Markdown.

        Execution Plan Format:

        {
          "steps": [
            {
              "skill": "SKILL_NAME",
              "operation": "OPERATION_NAME",
              "parameters": {}
            }
          ]
        }

        """ + promptBuilder.buildSkills(possibleSkills)
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
            throw new RuntimeException(
                    "Failed to parse execution plan: " + e.getMessage()
            );
        }
    }
}
