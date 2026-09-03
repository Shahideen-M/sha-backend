package com.sha.agentsData.agents.contentcreator.dto;

import com.sha.agentsData.agents.contentcreator.enums.ContentCreatorOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentCreatorRequest {
    private String content;
    private ContentCreatorOperation operation;
}
