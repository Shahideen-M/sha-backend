package com.sha.agents.developer.dto;

import com.sha.agents.developer.enums.DeveloperOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperRequest {

    private String projectPath;
    private DeveloperOperation operation;
    private String task;
}