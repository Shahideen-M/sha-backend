package com.sha.agentsData.agents.contentcreator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractedFrame {
    private double timestamp;
    private String filePath;
}
