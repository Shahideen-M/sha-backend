package com.sha.agentsData.agents.contentcreator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEditorResponse {
    private boolean success;
    private String message;
    private Double duration;
    private List<String> operations;
    private VideoAnalysisData analysis;
    private EditPlan editPlan;
    private NarrationScript narrationScript;
    private boolean requiresApproval;
    private String status;
    private String longVideoPath;
    private String shortVideoPath;
}
