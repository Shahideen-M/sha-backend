package com.sha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectReaderResponse {

    private String projectName;
    private List<String> javaFiles;
    private List<String> resourceFiles;
    private List<String> configurationFiles;
    private List<String> allFiles;

    private List<String> matchingFiles;

    public ProjectReaderResponse(List<String> matchingFiles) {
        this.matchingFiles = matchingFiles;
    }

    public ProjectReaderResponse(String projectName, List<String> javaFiles,
                                 List<String> resourceFiles, List<String> configurationFiles,
                                 List<String> allFiles) {
        this.projectName = projectName;
        this.javaFiles = javaFiles;
        this.resourceFiles = resourceFiles;
        this.configurationFiles = configurationFiles;
        this.allFiles = allFiles;
    }
}
