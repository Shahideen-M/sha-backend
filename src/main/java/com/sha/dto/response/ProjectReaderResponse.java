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
}
