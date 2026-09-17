package com.sha.skills.tools;

import com.sha.skills.ProjectReaderSkill;
import com.sha.skills.Skill;
import com.sha.skills.dto.request.ProjectReaderRequest;
import com.sha.skills.dto.response.ProjectReaderResponse;
import com.sha.skills.enums.ScanProjectOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Getter
@Component
@RequiredArgsConstructor
public class ProjectReaderTools implements ShaTool{

    private final ProjectReaderSkill projectReaderSkill;
    private final ObjectMapper objectMapper;

    @Override
    public Skill<?, ?> getSkill() {
        return projectReaderSkill;
    }

    @Override
    public Object createRequest(String toolName, String arguments) {

        ProjectReaderRequest request = new ProjectReaderRequest();
        var json = objectMapper.readTree(arguments);
        request.setProjectPath(json.get("projectPath").asString());

        switch (toolName) {
            case "scanProject" -> request.setOperation(ScanProjectOperation.SCAN_PROJECT);
            case "findFile" -> {
                request.setFileName(json.get("fileName").asString());
                request.setOperation(ScanProjectOperation.FIND_FILE);
            }
            case "findText" -> {
                request.setSearchText(json.get("searchText").asString());
                request.setOperation(ScanProjectOperation.FIND_TEXT);
            }
            default -> throw new IllegalArgumentException("Unknown ProjectReaderTools operation: " + toolName);
        }
        return request;
    }

    @Tool(description = "Scan a software project and return its files grouped by type")
    public ProjectReaderResponse scanProject(String projectPath) {
        ProjectReaderRequest request = new ProjectReaderRequest();
        request.setProjectPath(projectPath);
        request.setOperation(ScanProjectOperation.SCAN_PROJECT);
        return projectReaderSkill.execute(request);
    }

    @Tool(description = "Find files with an exact filename inside a software project")
    public ProjectReaderResponse findFile(String projectPath, String fileName) {
        ProjectReaderRequest request = new ProjectReaderRequest();
        request.setProjectPath(projectPath);
        request.setFileName(fileName);
        request.setOperation(ScanProjectOperation.FIND_FILE);
        return projectReaderSkill.execute(request);
    }

    @Tool(description = "Search for text inside files of a software project")
    public ProjectReaderResponse findText(String projectPath, String searchText) {
        ProjectReaderRequest request = new ProjectReaderRequest();
        request.setProjectPath(projectPath);
        request.setSearchText(searchText);
        request.setOperation(ScanProjectOperation.FIND_TEXT);
        return projectReaderSkill.execute(request);
    }
}