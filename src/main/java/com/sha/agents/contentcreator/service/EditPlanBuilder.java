package com.sha.agents.contentcreator.service;

import com.sha.agents.contentcreator.dto.EditPlan;
import com.sha.agents.contentcreator.dto.VideoAnalysisData;
import com.sha.agents.contentcreator.dto.VideoSegment;
import com.sha.agents.contentcreator.enums.RecommendedAction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EditPlanBuilder {

    public EditPlan buildPlan(VideoAnalysisData analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("Video analysis is required");
        }

        List<VideoSegment> segments = analysis.getSegments();

        if (segments == null || segments.isEmpty()) {
            return new EditPlan(
                    List.of(),
                    0.0,
                    0,
                    0,
                    "No valid segments available for editing."
            );
        }

        List<VideoSegment> usableSegments = new ArrayList<>();
        int removedCount = 0;

        for (VideoSegment segment : segments) {
            if (!isValidSegment(segment)) {
                continue;
            }

            if (segment.getAction() == RecommendedAction.REMOVE) {
                removedCount++;
                continue;
            }

            if (segment.getAction() == RecommendedAction.KEEP
                    || segment.getAction() == RecommendedAction.REVIEW) {
                usableSegments.add(segment);
            }
        }

        usableSegments.sort(
                Comparator.comparingDouble(VideoSegment::getStartTime)
        );

        List<VideoSegment> mergedSegments =
                mergeOverlappingSegments(usableSegments);

        double estimatedDuration =
                mergedSegments.stream()
                        .mapToDouble(segment ->
                                segment.getEndTime() - segment.getStartTime())
                        .sum();

        String summary = String.format(
                "Edit plan created: keep %d segments (%.1f seconds), remove %d segments. Review segments are preserved.",
                mergedSegments.size(),
                estimatedDuration,
                removedCount
        );

        return new EditPlan(
                mergedSegments,
                estimatedDuration,
                mergedSegments.size(),
                removedCount,
                summary
        );
    }

    private List<VideoSegment> mergeOverlappingSegments(
            List<VideoSegment> segments
    ) {
        if (segments.isEmpty()) {
            return List.of();
        }

        List<VideoSegment> merged = new ArrayList<>();
        VideoSegment current = segments.get(0);

        for (int i = 1; i < segments.size(); i++) {
            VideoSegment next = segments.get(i);

            if (next.getStartTime() <= current.getEndTime() + 0.05) {
                current.setEndTime(
                        Math.max(
                                current.getEndTime(),
                                next.getEndTime()
                        )
                );

                if ((current.getDescription() == null
                        || current.getDescription().isBlank())
                        && next.getDescription() != null) {
                    current.setDescription(next.getDescription());
                }

                if (current.getAction() != RecommendedAction.REVIEW
                        && next.getAction() == RecommendedAction.REVIEW) {
                    current.setAction(RecommendedAction.REVIEW);
                }
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return merged;
    }

    private boolean isValidSegment(VideoSegment segment) {
        return segment != null
                && Double.isFinite(segment.getStartTime())
                && Double.isFinite(segment.getEndTime())
                && segment.getStartTime() >= 0
                && segment.getEndTime() > segment.getStartTime();
    }
}