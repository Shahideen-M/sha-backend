package com.sha.agentsData.agents.contentcreator;

import com.sha.agentsData.agents.contentcreator.dto.ContentCreatorRequest;
import com.sha.agentsData.agents.contentcreator.dto.ContentCreatorResponse;
import com.sha.agentsData.agents.contentcreator.dto.VideoPlanData;
import com.sha.agentsData.agents.contentcreator.enums.ContentCreatorOperation;
import com.sha.agentsData.enums.AgentType;
import com.sha.agentsData.service.Agent;
import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.AgentPrompt;
import com.sha.dto.request.ChatRequest;
import com.sha.service.impl.AIRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentCreatorAgent implements Agent<ContentCreatorRequest, ContentCreatorResponse> {

    private final AIRouter aiRouter;
    private final ObjectMapper objectMapper;

    @Override
    public AgentType getType() {
        return AgentType.CONTENT_CREATOR;
    }

    @Override
    public ContentCreatorResponse executeTyped(ContentCreatorRequest request) {
        if (request == null || request.getOperation() == null) {
            return errorResponse("Content creator operation is required.");
        }

        return switch (request.getOperation()) {
            case VIDEO_PLAN -> generateVideoPlan(request);
        };
    }

    @Override
    public Class<ContentCreatorRequest> getRequestClass() {
        return ContentCreatorRequest.class;
    }

    @Override
    public ContentCreatorResponse execute(Object request) {
        return executeTyped((ContentCreatorRequest) request);
    }

    @Override
    public AgentPrompt<ContentCreatorOperation> describe() {

        return new AgentPrompt<>(
                AgentType.CONTENT_CREATOR,
                "Create video content plans including scripts, recording steps, visuals, editing notes, and YouTube metadata.",
                List.of(
                        "create video",
                        "video plan",
                        "content plan",
                        "youtube video",
                        "make a video",
                        "video content",
                        "script for video"
                ),
                List.of(
                        new OperationPrompt<>(
                                ContentCreatorOperation.VIDEO_PLAN,
                                "Create a complete video plan based on a topic.",
                                List.of("content"),
                                """
                                {
                                  "content":"How Sha Project Reader works",
                                  "operation":"VIDEO_PLAN"
                                }
                                """
                        )
                )
        );
    }

    private ContentCreatorResponse generateVideoPlan(ContentCreatorRequest request) {

        if (request.getContent() == null || request.getContent().isBlank()) return errorResponse("Video topic is required.");

        try {

            String prompt = buildPlanPrompt(request.getContent());

            var response = aiRouter.geminiChat(new ChatRequest(prompt));

            String json = extractJson(response.getResponse());

            VideoPlanData plan = objectMapper.readValue(json, VideoPlanData.class);

            return new ContentCreatorResponse(
                    true,
                    "Video plan generated successfully. Please review and approve.",
                    plan,
                    true,
                    "PLAN_READY"
            );

        } catch (Exception e) {
            return errorResponse("Failed to generate video plan: " + e.getMessage());
        }
    }

    private String buildPlanPrompt(String topic) {

        return """
                You are a professional content creator.

                Create a complete video plan based on the user's topic.

                Return ONLY valid JSON.
                Do not use markdown.
                Do not add explanations before or after the JSON.

                Use exactly this structure:

                {
                  "title": "string",
                  "topic": "string",
                  "hook": "string",
                  "recordingSteps": ["string"],
                  "script": "string",
                  "visuals": ["string"],
                  "editingNotes": ["string"],
                  "youtubeTitle": "string",
                  "youtubeDescription": "string",
                  "shortVersion": "string"
                }

                User topic:
                """
                + topic;
    }

    private String extractJson(String response) {

        if (response == null || response.isBlank()) throw new IllegalArgumentException("AI returned an empty response.");

        String text = response.trim();

        if (text.startsWith("```")) {

            int firstNewLine = text.indexOf('\n');

            if (firstNewLine != -1) {
                text = text.substring(firstNewLine + 1);
            }

            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
        }

        int start = text.indexOf('{');

        int end = text.lastIndexOf('}');

        if (start == -1 || end == -1 || end <= start) throw new IllegalArgumentException("AI did not return valid JSON.");

        return text.substring(start, end + 1).trim();
    }

    private ContentCreatorResponse errorResponse(String message) {

        return new ContentCreatorResponse(
                false,
                message,
                null,
                false,
                "ERROR"
        );
    }
}