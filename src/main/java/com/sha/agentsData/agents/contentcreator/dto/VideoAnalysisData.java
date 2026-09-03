package com.sha.agentsData.agents.contentcreator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoAnalysisData {
    private double duration;
    private String resolution;
    private String codec;
    private List<VideoSegment> segments;
    private String summary;
    private int totalSegments;
    private int segmentsToKeep;
    private int segmentsToRemove;
}
