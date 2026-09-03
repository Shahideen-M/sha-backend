package com.sha.agentsData.agents.contentcreator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoTimeline {
    private double duration;
    private String videoPath;
    private List<ExtractedFrame> frames;
    private Map<String, String> metadata;
}
