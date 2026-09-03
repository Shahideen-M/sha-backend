package com.sha.agentsData.agents.contentcreator.dto;

import com.sha.agentsData.agents.contentcreator.enums.VideoEditorOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEditorRequest {
    private VideoEditorOperation operation;
    private String videoPath;
    private String outputPath;
    private String context;
    private String instructions;
    private String introPath;
    private Boolean generateNarration;
    private VideoPlanData videoPlan;
    private VideoAnalysisData analysis;
    private EditPlan editPlan;
    private NarrationScript narrationScript;
}
