package com.sha.service.skills;

import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.dto.request.DeveloperAssistantRequest;
import com.sha.dto.response.DeveloperAssistantResponse;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import com.sha.service.impl.AIRouter;
import org.springframework.stereotype.Service;

@Service
public class DeveloperAssistantSkill implements Skill<DeveloperAssistantRequest, DeveloperAssistantResponse> {

    private final AIRouter aiRouter;

    public DeveloperAssistantSkill(AIRouter aiRouter) {
        this.aiRouter = aiRouter;
    }

    @Override
    public SkillType getType() {
        return SkillType.AI;
    }

    @Override
    public DeveloperAssistantResponse execute(DeveloperAssistantRequest request) {

        String userInput = request.getContent();

        String prompt = switch (request.getTaskType()) {
            case GENERAL -> "Answer the following software engineering question:\n" + userInput;

            case REVIEW_CODE -> "Review the following code and suggest improvements:\n" + userInput;

            case SPRING_BOOT -> "Answer the following Spring Boot question:\n" + userInput;

            case EXPLAIN_CODE -> "Explain the following code:\n" + userInput;

            case EXPLAIN_ERROR -> "Explain the following error and suggest a fix:\n" + userInput;

            case GENERATE_DTO -> "Generate a Java 21 Spring Boot DTO:\n" + userInput;

            case GENERATE_ENTITY -> "Generate a Java 21 Spring Boot JPA Entity. Return only Java code.\n" + userInput;

            case GENERATE_SERVICE -> "Generate a Spring Boot Service. Return only Java code.\n" + userInput;

            case GENERATE_CONTROLLER -> "Generate a Spring Boot REST Controller. Return only Java code.:\n" + userInput;

            case GENERATE_REPOSITORY -> "Generate a Spring Data JPA Repository. Return only Java code:\n" + userInput;

            default -> request.getContent();

        };

        ChatRequest chatRequest = new ChatRequest(prompt);
        ChatResponse chatResponse = aiRouter.chat(chatRequest);

        return new DeveloperAssistantResponse(chatResponse.getResponse());
    }
}
