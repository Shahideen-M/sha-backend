package com.sha.brain.dto;

import tools.jackson.databind.JsonNode;
import com.sha.brain.enums.ShaResponseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShaBrainResponse {

    private ShaResponseType type;
    private String message;
    private JsonNode data;

    private Boolean approvalRequired = false;
    private String approvalToken = "";

    private JsonNode action;

    public ShaBrainResponse(
            ShaResponseType type,
            String message,
            JsonNode data,
            Boolean approvalRequired,
            String approvalToken
    ) {
        this.type = type;
        this.message = message;
        this.data = data;
        this.approvalRequired = approvalRequired;
        this.approvalToken = approvalToken;
        this.action = null;
    }
}
