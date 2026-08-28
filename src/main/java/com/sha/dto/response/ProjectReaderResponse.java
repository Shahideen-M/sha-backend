package com.sha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectReaderResponse {

    private boolean success;
    private String message;
    private String projectName;
    private List<String> javaFiles;
    private List<String> resourceFiles;
    private List<String> configurationFiles;
    private List<String> allFiles;

    private List<String> matchingFiles;

    public ProjectReaderResponse(boolean success, String message, List<String> matchingFiles) {
        this.success = success;
        this.message = message;
        this.matchingFiles = matchingFiles;
    }

    public ProjectReaderResponse(boolean success, String message, String projectName, List<String> javaFiles,
                                 List<String> resourceFiles, List<String> configurationFiles,
                                 List<String> allFiles) {
        this.success = success;
        this.message = message;
        this.projectName = projectName;
        this.javaFiles = javaFiles;
        this.resourceFiles = resourceFiles;
        this.configurationFiles = configurationFiles;
        this.allFiles = allFiles;
    }
}
