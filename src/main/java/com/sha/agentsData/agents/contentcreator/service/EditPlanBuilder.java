package com.sha.agentsData.agents.contentcreator.service;

import com.sha.agentsData.agents.contentcreator.dto.EditPlan;
import com.sha.agentsData.agents.contentcreator.dto.VideoAnalysisData;
import com.sha.agentsData.agents.contentcreator.dto.VideoSegment;
import com.sha.agentsData.agents.contentcreator.enums.RecommendedAction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EditPlanBuilder {

    public EditPlan buildPlan(
            VideoAnalysisData analysis
    ) {

        if (analysis == null) {

            throw new IllegalArgumentException(
                    "Video analysis is required"
            );
        }

        List<VideoSegment> allSegments =
                analysis.getSegments();

        if (allSegments == null
                || allSegments.isEmpty()) {

            return new EditPlan(
                    List.of(),
                    0.0,
                    0,
                    0,
                    "No valid segments available for editing."
            );
        }

        List<VideoSegment> keepSegments =
                new ArrayList<>();

        int removedCount = 0;

        for (VideoSegment segment : allSegments) {

            if (!isValidSegment(segment)) {
                continue;
            }

            if (segment.getAction()
                    == RecommendedAction.KEEP) {

                keepSegments.add(segment);

            } else if (segment.getAction()
                    == RecommendedAction.REMOVE) {

                removedCount++;
            }
        }

        keepSegments.sort(
                Comparator.comparingDouble(
                        VideoSegment::getStartTime
                )
        );

        double estimatedDuration =
                keepSegments.stream()
                        .mapToDouble(segment ->
                                segment.getEndTime()
                                        - segment.getStartTime()
                        )
                        .sum();

        String summary =
                String.format(
                        "Edit plan created: keep %d segments "
                                + "(%.1f seconds), "
                                + "remove %d segments. "
                                + "Review segments are not "
                                + "automatically included.",
                        keepSegments.size(),
                        estimatedDuration,
                        removedCount
                );

        return new EditPlan(
                keepSegments,
                estimatedDuration,
                keepSegments.size(),
                removedCount,
                summary
        );
    }

    private boolean isValidSegment(
            VideoSegment segment
    ) {

        if (segment == null) {
            return false;
        }

        return segment.getStartTime() >= 0
                && segment.getEndTime()
                > segment.getStartTime();
    }
}