package com.sha.agents.developer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperResponse {

    private boolean success;
    private String message;
    private boolean approvalRequired;
    private String approvalToken;
    private JsonNode plan;
    private JsonNode action;
}