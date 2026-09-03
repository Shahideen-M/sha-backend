package com.sha.agentsData.agents.contentcreator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditPlan {
    private List<VideoSegment> segments;
    private double estimatedDuration;
    private int segmentCount;
    private int removedSegmentCount;
    private String summary;
}
