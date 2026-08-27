package com.sha.service.skills;

import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.dto.request.DeveloperAssistantRequest;
import com.sha.dto.response.DeveloperAssistantResponse;
import com.sha.enums.DeveloperTaskType;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import com.sha.service.impl.AIRouter;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public DeveloperAssistantResponse executeTyped(DeveloperAssistantRequest request) {

        String userInput = request.getContent();

        String basePrompt =
                "You are Sha's Developer Assistant.\n" +
                        "\n" +
                        "You are an expert software engineer specializing in Java 21, Spring Boot, backend development, and software architecture.\n" +
                        "\n" +
                        "Rules:\n" +
                        "- Give technically accurate answers.\n" +
                        "- Analyze the problem before suggesting a solution.\n" +
                        "- Prefer clean, maintainable, production-quality solutions.\n" +
                        "- Respect the user's existing architecture and code.\n" +
                        "- Do not invent project details, classes, methods, or behavior.\n" +
                        "- When code is provided, base your answer on the provided code.\n" +
                        "- When explaining an error, identify the root cause first.\n" +
                        "- When reviewing code, identify important problems and explain why they are problems.\n" +
                        "- When generating code, produce complete and directly usable code.\n" +
                        "- Do not claim that code was executed or tested unless it actually was.\n";

        String prompt = basePrompt + "\n" + switch (request.getTaskType()) {
            case GENERAL ->
                    "Answer the following software engineering question clearly and accurately.\n" +
                    "Provide examples when they improve understanding.\n" +
                    "\n" +
                    "User request:\n" + userInput;

            case REVIEW_CODE ->
                    "Review the following code.\n" +
                    "\n" +
                    "Analyze:\n" +
                    "- Bugs and incorrect behavior\n" +
                    "- Design problems\n" +
                    "- Maintainability\n" +
                    "- Null-safety\n" +
                    "- Performance\n" +
                    "- Java and Spring Boot best practices\n" +
                    "\n" +
                    "Explain the important issues and provide improved code where useful.\n" +
                    "\n" +
                    "Code:\n" + userInput;

            case SPRING_BOOT ->
                    "Answer the following Spring Boot question.\n" +
                    "\n" +
                    "Use Java 21 and current Spring Boot best practices where applicable.\n" +
                    "Explain the reasoning behind the solution.\n" +
                    "\n" +
                    "User request:\n" + userInput;

            case EXPLAIN_CODE ->
                    "Explain the following code.\n" +
                    "\n" +
                    "Cover:\n" +
                    "- What it does\n" +
                    "- How it works\n" +
                    "- Important classes and methods\n" +
                    "- How the components interact\n" +
                    "- Potential problems or improvements\n" +
                    "\n" +
                    "Code:\n" + userInput;

            case EXPLAIN_ERROR ->
                    "Analyze the following error.\n" +
                    "\n" +
                    "Provide:\n" +
                    "1. Root cause\n" +
                    "2. Why it happened\n" +
                    "3. Exact fix\n" +
                    "4. Relevant improvements to prevent it from happening again\n" +
                    "\n" +
                    "Error:\n" + userInput;

            case GENERATE_DTO ->
                    "Generate a complete Java 21 Spring Boot DTO based on the following requirements.\n" +
                    "\n" +
                    "Use appropriate:\n" +
                    "- Fields\n" +
                    "- Types\n" +
                    "- Validation where appropriate\n" +
                    "- Constructors/accessors or the project's expected approach\n" +
                    "\n" +
                    "Return only Java code.\n" +
                    "\n" +
                    "Requirements:\n" + userInput;

            case GENERATE_ENTITY ->
                    "Generate a complete Java 21 Spring Boot JPA Entity based on the following requirements.\n" +
                    "\n" +
                    "Use appropriate JPA annotations, relationships, IDs, and types.\n" +
                    "Follow clean Spring Boot practices.\n" +
                    "\n" +
                    "Return only Java code.\n" +
                    "\n" +
                    "Requirements:\n" + userInput;

            case GENERATE_SERVICE ->
                    "Generate a complete Java 21 Spring Boot Service based on the following requirements.\n" +
                    "\n" +
                    "Use proper dependency injection and clean service-layer design.\n" +
                    "Follow Java 21 and Spring Boot best practices.\n" +
                    "\n" +
                    "Return only Java code.\n" +
                    "\n" +
                    "Requirements:\n" + userInput;

            case GENERATE_CONTROLLER ->
                    "Generate a complete Java 21 Spring Boot REST Controller based on the following requirements.\n" +
                    "\n" +
                    "Use appropriate HTTP methods, request/response handling, validation, and dependency injection.\n" +
                    "\n" +
                    "Return only Java code.\n" +
                    "\n" +
                    "Requirements:\n" + userInput;

            case GENERATE_REPOSITORY ->
                    "Generate a complete Spring Data JPA Repository based on the following requirements.\n" +
                            "\n" +
                            "Use the appropriate entity and ID types and add query methods only when required.\n" +
                            "\n" +
                            "Return only Java code.\n" +
                            "\n" +
                            "Requirements:\n" + userInput;

        };

        ChatRequest chatRequest = new ChatRequest(prompt);
        ChatResponse chatResponse = aiRouter.geminiChat(chatRequest);

        return new DeveloperAssistantResponse(chatResponse.getResponse());
    }

    @Override
    public Class<DeveloperAssistantRequest> getRequestClass() {
        return DeveloperAssistantRequest.class;
    }

    @Override
    public DeveloperAssistantResponse execute(Object request) {
        return executeTyped((DeveloperAssistantRequest) request);
    }

    @Override
    public SkillPrompt<DeveloperTaskType> describe() {
        return new SkillPrompt<>(
                SkillType.AI,
                "Provides software engineering assistance for Java 21, Spring Boot, backend development, code analysis, debugging, and code generation.",
                List.of(
                        "code",
                        "coding",
                        "programming",
                        "java",
                        "spring boot",
                        "spring",
                        "developer",
                        "development",
                        "debug",
                        "error",
                        "exception",
                        "bug",
                        "review code",
                        "explain code",
                        "generate code",
                        "dto",
                        "entity",
                        "controller",
                        "service",
                        "repository"
                ),
                List.of()
        );
    }
}
