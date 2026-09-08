package com.sha.agents.contentcreator.dto;

import com.sha.agents.contentcreator.enums.RecommendedAction;
import com.sha.agents.contentcreator.enums.VideoSegmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoSegment {
    private double startTime;
    private double endTime;
    private VideoSegmentType type;
    private RecommendedAction action;
    private double confidence;
    private String description;
    private String expectedStep;

    public VideoSegment(double startTime, double endTime, String description) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }
}
