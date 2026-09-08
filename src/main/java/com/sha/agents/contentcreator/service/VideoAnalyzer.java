package com.sha.agents.contentcreator.service;

import com.sha.agents.contentcreator.dto.ExtractedFrame;
import com.sha.agents.contentcreator.dto.VideoAnalysisData;
import com.sha.agents.contentcreator.dto.VideoPlanData;
import com.sha.agents.contentcreator.dto.VideoSegment;
import com.sha.agents.contentcreator.dto.VideoTimeline;
import com.sha.agents.contentcreator.enums.RecommendedAction;
import com.sha.agents.contentcreator.enums.VideoSegmentType;
import com.sha.skills.dto.request.ImageAnalysisRequest;
import com.sha.skills.service.impl.AIRouter;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class VideoAnalyzer {

    private final AIRouter aiRouter;
    private final ObjectMapper objectMapper;

    public VideoAnalyzer(AIRouter aiRouter, ObjectMapper objectMapper) {
        this.aiRouter = aiRouter;
        this.objectMapper = objectMapper;
    }

    public VideoAnalysisData analyze(
            VideoTimeline timeline,
            VideoPlanData plan
    ) {
        try {
            if (timeline == null) {
                throw new IllegalArgumentException("Video timeline is required");
            }

            List<ExtractedFrame> frames = timeline.getFrames();

            if (frames == null || frames.isEmpty()) {
                return fallbackAnalysis(
                        timeline,
                        "No video frames were available for analysis."
                );
            }

            String prompt = buildAnalysisPrompt(timeline, plan);

            List<String> imagePaths = frames.stream()
                    .map(ExtractedFrame::getFilePath)
                    .filter(path -> path != null && !path.isBlank())
                    .toList();

            if (imagePaths.isEmpty()) {
                return fallbackAnalysis(
                        timeline,
                        "No valid frame files were available for analysis."
                );
            }

            ImageAnalysisRequest request =
                    new ImageAnalysisRequest(prompt, imagePaths);

            String response =
                    aiRouter
                            .geminiAnalyzeImages(request)
                            .getResponse();

            String json = extractJson(response);

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
                    "AI video analysis failed: " + e.getMessage()
            );
        }
    }

    private String buildAnalysisPrompt(
            VideoTimeline timeline,
            VideoPlanData plan
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are analyzing extracted frames from a video.

                The images are provided in chronological order.
                Each image represents a moment in the original video.

                Analyze ONLY what can reasonably be determined from the
                visible frames. Do not invent events or actions.

                The goal is to create a SAFE editing plan for a long-form
                YouTube video.

                IMPORTANT:
                - Preserve useful content.
                - Do NOT remove content merely because the same scene appears
                  across multiple frames.
                - Do NOT remove Quran recitation, meaningful gameplay,
                  demonstrations, explanations, or important visual content.
                - Only recommend REMOVE when a pause, error, unnecessary wait,
                  obvious failed attempt, or clearly useless section is
                  reasonably identifiable.
                - When uncertain, use REVIEW.
                - Prefer REVIEW over REMOVE.
                - Keep continuous meaningful content intact.

                Segment types:
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

                Actions:
                KEEP
                REMOVE
                REVIEW

                Return ONLY valid JSON.
                Do not use markdown.
                Do not include explanations outside JSON.

                """);

        prompt.append("Video duration: ")
                .append(timeline.getDuration())
                .append(" seconds\n\n");

        prompt.append("Frames:\n");

        for (int i = 0; i < timeline.getFrames().size(); i++) {
            ExtractedFrame frame = timeline.getFrames().get(i);

            prompt.append("Image ")
                    .append(i + 1)
                    .append(" timestamp: ")
                    .append(frame.getTimestamp())
                    .append(" seconds\n");
        }

        if (plan != null
                && plan.getRecordingSteps() != null
                && !plan.getRecordingSteps().isEmpty()) {

            prompt.append("\nExpected recording steps:\n");

            for (int i = 0;
                 i < plan.getRecordingSteps().size();
                 i++) {

                prompt.append(i + 1)
                        .append(". ")
                        .append(plan.getRecordingSteps().get(i))
                        .append("\n");
            }
        }

        prompt.append("""
                
                Rules:
                - startTime >= 0
                - endTime > startTime
                - endTime <= video duration
                - confidence must be between 0 and 1
                - base decisions on visible evidence
                - use REVIEW when uncertain
                - keep useful content
                - avoid excessive fragmentation
                - avoid removing content based only on a single frame
                - obvious long pauses and waits may be REMOVE
                - obvious errors may be REMOVE
                - meaningful continuous sections should be KEEP

                Required JSON structure:

                {
                  "segments": [
                    {
                      "startTime": 0.0,
                      "endTime": 5.0,
                      "type": "OTHER",
                      "action": "KEEP",
                      "confidence": 0.8,
                      "description": "Description of visible content",
                      "expectedStep": null
                    }
                  ],
                  "summary": "Brief summary",
                  "totalSegments": 1,
                  "segmentsToKeep": 1,
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
                    timeline.getMetadata().get("resolution")
            );

            data.setCodec(
                    timeline.getMetadata().get("codec")
            );
        }
    }

    private void validateSegments(
            VideoAnalysisData data,
            double duration
    ) {
        if (data == null) {
            return;
        }

        if (data.getSegments() == null) {
            data.setSegments(new ArrayList<>());
            return;
        }

        List<VideoSegment> validSegments = new ArrayList<>();

        for (VideoSegment segment : data.getSegments()) {
            if (segment == null) {
                continue;
            }

            double start = segment.getStartTime();
            double end = segment.getEndTime();

            if (!Double.isFinite(start)
                    || !Double.isFinite(end)) {
                continue;
            }

            start = Math.max(0.0, start);
            end = Math.min(duration, end);

            if (end <= start) {
                continue;
            }

            segment.setStartTime(start);
            segment.setEndTime(end);

            if (segment.getType() == null) {
                segment.setType(VideoSegmentType.OTHER);
            }

            if (segment.getAction() == null) {
                segment.setAction(RecommendedAction.REVIEW);
            }

            double confidence = segment.getConfidence();

            if (!Double.isFinite(confidence)) {
                confidence = 0.5;
            }

            segment.setConfidence(
                    Math.clamp(confidence, 0.0, 1.0)
            );

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
        double duration =
                timeline != null
                        ? timeline.getDuration()
                        : 0.0;

        List<VideoSegment> segments = new ArrayList<>();

        if (duration > 0) {
            segments.add(
                    new VideoSegment(
                            0.0,
                            duration,
                            VideoSegmentType.OTHER,
                            RecommendedAction.REVIEW,
                            0.5,
                            "Entire video requires review",
                            null
                    )
            );
        }

        VideoAnalysisData data = new VideoAnalysisData();

        data.setDuration(duration);
        data.setSegments(segments);
        data.setSummary(reason);

        if (timeline != null
                && timeline.getMetadata() != null) {

            data.setResolution(
                    timeline.getMetadata().get("resolution")
            );

            data.setCodec(
                    timeline.getMetadata().get("codec")
            );
        }

        computeSummary(data);

        return data;
    }

    private void computeSummary(VideoAnalysisData data) {
        List<VideoSegment> segments = data.getSegments();

        if (segments == null) {
            data.setTotalSegments(0);
            data.setSegmentsToKeep(0);
            data.setSegmentsToRemove(0);
            return;
        }

        data.setTotalSegments(segments.size());

        data.setSegmentsToKeep(
                (int) segments.stream()
                        .filter(segment ->
                                segment.getAction() == RecommendedAction.KEEP
                                        || segment.getAction() == RecommendedAction.REVIEW
                        )
                        .count()
        );

        data.setSegmentsToRemove(
                (int) segments.stream()
                        .filter(segment ->
                                segment.getAction() == RecommendedAction.REMOVE
                        )
                        .count()
        );
    }

    private String extractJson(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException(
                    "AI returned an empty response"
            );
        }

        String trimmed = response.trim();

        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(
                    0,
                    trimmed.length() - 3
            );
        }

        return trimmed.trim();
    }
}