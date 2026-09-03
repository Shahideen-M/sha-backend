package com.sha.brain.dto;

import tools.jackson.databind.JsonNode;
import com.sha.brain.enums.ShaResponseType;
import com.sha.brain.enums.SkillType;
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
}
