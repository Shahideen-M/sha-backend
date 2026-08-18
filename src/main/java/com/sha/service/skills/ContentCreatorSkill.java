package com.sha.service.skills;

import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.data.VideoPlanData;
import com.sha.dto.request.ChatRequest;
import com.sha.dto.request.ContentCreatorRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.dto.response.ContentCreatorResponse;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import com.sha.service.impl.AIRouter;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class ContentCreatorSkill implements Skill<ContentCreatorRequest, ContentCreatorResponse> {

    private final AIRouter aiRouter;
    private final ObjectMapper objectMapper;

    public ContentCreatorSkill(AIRouter aiRouter, ObjectMapper objectMapper) {
        this.aiRouter = aiRouter;
        this.objectMapper = objectMapper;
    }

    @Override
    public SkillType getType() {
        return SkillType.CONTENT_CREATOR;
    }

    @Override
    public ContentCreatorResponse executeTyped(ContentCreatorRequest request) {
        return switch (request.getOperation()) {
            case VIDEO_PLAN -> videoPlan(request);
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
    public SkillPrompt<?> describe() {
        return null;
    }

    public ContentCreatorResponse videoPlan(ContentCreatorRequest request) {

        String prompt = """
            You are a professional content creator assistant.

            Create a video plan based on the user's topic.

            Return ONLY valid JSON.
            Do not use Markdown.
            Do not use ```json.

            The JSON must match this structure exactly:

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
            """ + request.getContent();
        ChatResponse response = aiRouter.chat(
                new ChatRequest(prompt)
        );

        try {
            VideoPlanData data = objectMapper.readValue(
                    response.getResponse(),
                    VideoPlanData.class
            );

            return new ContentCreatorResponse(
                    true,
                    "Video plan generated successfully",
                    data
            );

        } catch (Exception e) {
            return new ContentCreatorResponse(
                    false,
                    "Failed to generate video plan: " + e.getMessage(),
                    null
            );
        }
    }

}
