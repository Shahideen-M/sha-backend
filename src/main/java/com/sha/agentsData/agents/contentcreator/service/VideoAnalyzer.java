package com.sha.agentsData.agents.contentcreator.service;

import com.sha.agentsData.agents.contentcreator.dto.VideoAnalysisData;
import com.sha.agentsData.agents.contentcreator.dto.VideoPlanData;
import com.sha.agentsData.agents.contentcreator.dto.VideoSegment;
import com.sha.agentsData.agents.contentcreator.dto.VideoTimeline;
import com.sha.agentsData.agents.contentcreator.enums.RecommendedAction;
import com.sha.agentsData.agents.contentcreator.enums.VideoSegmentType;
import com.sha.dto.request.ChatRequest;
import com.sha.service.impl.AIRouter;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class VideoAnalyzer {

    private final AIRouter aiRouter;
    private final ObjectMapper objectMapper;

    public VideoAnalyzer(
            AIRouter aiRouter,
            ObjectMapper objectMapper
    ) {
        this.aiRouter = aiRouter;
        this.objectMapper = objectMapper;
    }

    public VideoAnalysisData analyze(
            VideoTimeline timeline,
            VideoPlanData plan
    ) {

        try {

            String prompt =
                    buildAnalysisPrompt(timeline, plan);

            ChatRequest request =
                    new ChatRequest(prompt);

            var response =
                    aiRouter.geminiChat(request);

            String json =
                    extractJson(response.getResponse());

            VideoAnalysisData data =
                    objectMapper.readValue(
                            json,
                            VideoAnalysisData.class
                    );

            enrichMetadata(data, timeline);

            validateSegments(data, timeline.getDuration());

            computeSummary(data);

            return data;

        } catch (Exception e) {

            return fallbackAnalysis(
                    timeline,
                    "AI analysis unavailable. "
                            + "Manual review recommended."
            );
        }
    }

    private String buildAnalysisPrompt(
            VideoTimeline timeline,
            VideoPlanData plan
    ) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append("""
                You are helping create a suggested edit analysis
                for a screen recording.

                IMPORTANT:
                You do NOT have direct access to the video frames
                or visual image content.

                Do not claim that you saw anything on the screen.

                You only know:
                - video duration
                - metadata
                - extracted frame timestamps
                - the expected recording plan, if provided

                Your job is to create a conservative suggested
                timeline for human review.

                When you cannot confidently determine what happened,
                use:
                type = OTHER
                action = REVIEW

                Never invent specific screen events.

                """);

        prompt.append("Video duration: ")
                .append(timeline.getDuration())
                .append(" seconds\n");

        if (timeline.getMetadata() != null) {

            prompt.append("Resolution: ")
                    .append(
                            timeline.getMetadata()
                                    .getOrDefault(
                                            "resolution",
                                            "unknown"
                                    )
                    )
                    .append("\n");

            prompt.append("Codec: ")
                    .append(
                            timeline.getMetadata()
                                    .getOrDefault(
                                            "codec",
                                            "unknown"
                                    )
                    )
                    .append("\n");
        }

        if (timeline.getFrames() != null
                && !timeline.getFrames().isEmpty()) {

            prompt.append(
                    "\nAvailable frame timestamps:\n"
            );

            timeline.getFrames()
                    .forEach(frame ->
                            prompt.append(
                                    String.format(
                                            "%.1f, ",
                                            frame.getTimestamp()
                                    )
                            )
                    );

            prompt.append("\n");
        }

        if (plan != null
                && plan.getRecordingSteps() != null
                && !plan.getRecordingSteps().isEmpty()) {

            prompt.append(
                    "\nExpected recording steps:\n"
            );

            for (int i = 0;
                 i < plan.getRecordingSteps().size();
                 i++) {

                prompt.append(i + 1)
                        .append(". ")
                        .append(
                                plan.getRecordingSteps()
                                        .get(i)
                        )
                        .append("\n");
            }
        }

        prompt.append("""

                Create conservative suggested segments.

                Segment type must be one of:
                SUCCESSFUL_ATTEMPT
                FAILED_ATTEMPT
                REPEATED_ATTEMPT
                PAUSE
                LONG_WAIT
                ERROR
                UNNECESSARY
                SETUP
                INTRO
                OUTRO
                OTHER

                Action must be one of:
                KEEP
                REMOVE
                REVIEW

                Use REVIEW whenever confidence is low.

                Rules:
                - startTime must be >= 0
                - endTime must be greater than startTime
                - endTime must not exceed video duration
                - confidence must be between 0 and 1
                - do not invent visual events
                - conservative suggestions are preferred

                Return ONLY valid JSON.

                Required JSON structure:

                {
                  "segments": [
                    {
                      "startTime": 0.0,
                      "endTime": 5.0,
                      "type": "OTHER",
                      "action": "REVIEW",
                      "confidence": 0.5,
                      "description": "Suggested segment requiring review",
                      "expectedStep": null
                    }
                  ],
                  "summary": "Brief summary",
                  "totalSegments": 1,
                  "segmentsToKeep": 0,
                  "segmentsToRemove": 0
                }
                """);

        return prompt.toString();
    }

    private void enrichMetadata(
            VideoAnalysisData data,
            VideoTimeline timeline
    ) {

        data.setDuration(timeline.getDuration());

        if (timeline.getMetadata() != null) {

            data.setResolution(
                    timeline.getMetadata()
                            .get("resolution")
            );

            data.setCodec(
                    timeline.getMetadata()
                            .get("codec")
            );
        }
    }

    private void validateSegments(
            VideoAnalysisData data,
            double duration
    ) {

        if (data.getSegments() == null) {

            data.setSegments(new ArrayList<>());

            return;
        }

        List<VideoSegment> validSegments =
                new ArrayList<>();

        for (VideoSegment segment :
                data.getSegments()) {

            if (segment == null) {
                continue;
            }

            double start =
                    Math.max(0, segment.getStartTime());

            double end =
                    Math.min(
                            duration,
                            segment.getEndTime()
                    );

            if (end <= start) {
                continue;
            }

            segment.setStartTime(start);
            segment.setEndTime(end);

            if (segment.getType() == null) {
                segment.setType(
                        VideoSegmentType.OTHER
                );
            }

            if (segment.getAction() == null) {
                segment.setAction(
                        RecommendedAction.REVIEW
                );
            }

            double confidence =
                    Math.max(
                            0.0,
                            Math.min(
                                    1.0,
                                    segment.getConfidence()
                            )
                    );

            segment.setConfidence(confidence);

            validSegments.add(segment);
        }

        validSegments.sort(
                Comparator.comparingDouble(
                        VideoSegment::getStartTime
                )
        );

        data.setSegments(validSegments);
    }

    private VideoAnalysisData fallbackAnalysis(
            VideoTimeline timeline,
            String reason
    ) {

        List<VideoSegment> segments =
                new ArrayList<>();

        double duration =
                timeline.getDuration();

        if (duration > 0) {

            segments.add(
                    new VideoSegment(
                            0.0,
                            duration,
                            VideoSegmentType.OTHER,
                            RecommendedAction.REVIEW,
                            0.5,
                            "Entire video requires manual review",
                            null
                    )
            );
        }

        VideoAnalysisData data =
                new VideoAnalysisData();

        data.setDuration(duration);

        data.setSegments(segments);

        data.setSummary(reason);

        if (timeline.getMetadata() != null) {

            data.setResolution(
                    timeline.getMetadata()
                            .get("resolution")
            );

            data.setCodec(
                    timeline.getMetadata()
                            .get("codec")
            );
        }

        computeSummary(data);

        return data;
    }

    private void computeSummary(
            VideoAnalysisData data
    ) {

        List<VideoSegment> segments =
                data.getSegments();

        if (segments == null) {

            data.setTotalSegments(0);
            data.setSegmentsToKeep(0);
            data.setSegmentsToRemove(0);

            return;
        }

        data.setTotalSegments(
                segments.size()
        );

        data.setSegmentsToKeep(
                (int) segments.stream()
                        .filter(segment ->
                                segment.getAction()
                                        == RecommendedAction.KEEP
                        )
                        .count()
        );

        data.setSegmentsToRemove(
                (int) segments.stream()
                        .filter(segment ->
                                segment.getAction()
                                        == RecommendedAction.REMOVE
                        )
                        .count()
        );
    }

    private String extractJson(String response) {

        if (response == null) {
            throw new IllegalArgumentException(
                    "AI returned an empty response"
            );
        }

        String trimmed =
                response.trim();

        if (trimmed.startsWith("```json")) {

            trimmed =
                    trimmed.substring(7);

        } else if (trimmed.startsWith("```")) {

            trimmed =
                    trimmed.substring(3);
        }

        if (trimmed.endsWith("```")) {

            trimmed =
                    trimmed.substring(
                            0,
                            trimmed.length() - 3
                    );
        }

        return trimmed.trim();
    }
}