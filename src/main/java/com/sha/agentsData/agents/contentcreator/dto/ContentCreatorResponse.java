package com.sha.agentsData.agents.contentcreator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentCreatorResponse {
    private boolean success;
    private String message;
    private VideoPlanData data;
    private boolean requiresApproval;
    private String status;
}
