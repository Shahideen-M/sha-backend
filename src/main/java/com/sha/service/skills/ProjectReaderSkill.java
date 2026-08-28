package com.sha.service.skills;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.request.ProjectReaderRequest;
import com.sha.dto.response.ProjectReaderResponse;
import com.sha.enums.ScanProjectOperation;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ProjectReaderSkill implements Skill<ProjectReaderRequest, ProjectReaderResponse> {

    @Override
    public SkillType getType() {
        return SkillType.PROJECT;
    }

    @Override
    public ProjectReaderResponse executeTyped(ProjectReaderRequest request) {
        return switch (request.getOperation()) {

            case SCAN_PROJECT -> scanProject(request);

            case FIND_FILE -> findFile(request);

            case FIND_TEXT -> findText(request);
        };
    }

    @Override
    public Class<ProjectReaderRequest> getRequestClass() {
        return ProjectReaderRequest.class;
    }

    @Override
    public ProjectReaderResponse execute(Object request) {
        return executeTyped((ProjectReaderRequest) request);
    }

    @Override
    public SkillPrompt<ScanProjectOperation> describe() {
        return new SkillPrompt<>(
                SkillType.PROJECT,
                "Read and analyze software projects.",
                List.of(
                        "project",
                        "codebase",
                        "source code",
                        "repository",
                        "repo",
                        "scan project",
                        "find file",
                        "find text",
                        "search code"
                ),
                List.of(
                        new OperationPrompt<>(
                                ScanProjectOperation.SCAN_PROJECT,
                                "Scan an entire software project.",
                                List.of("projectPath"),
                                """
                                {
                                  "projectPath":"D:/Projects/Sha",
                                  "operation":"SCAN_PROJECT"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                ScanProjectOperation.FIND_FILE,
                                "Find a file in the project.",
                                List.of("projectPath", "fileName"),
                                """
                                {
                                  "projectPath":"D:/Projects/Sha",
                                  "fileName":"ChatController.java",
                                  "operation":"FIND_FILE"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                ScanProjectOperation.FIND_TEXT,
                                "Search text inside all project files.",
                                List.of("projectPath", "searchText"),
                                """
                                {
                                  "projectPath":"D:/Projects/Sha",
                                  "searchText":"SkillRegistry",
                                  "operation":"FIND_TEXT"
                                }
                                """
                        )
                )
        );
    }

    public ProjectReaderResponse scanProject(ProjectReaderRequest request) {

        try {
            Path path = getProjectPath(request);
            validate(path);

            String projectName = path.getFileName().toString();
            List<String> javaFiles = new ArrayList<>();
            List<String> resourceFiles = new ArrayList<>();
            List<String> configurationFiles = new ArrayList<>();
            List<String> allFiles = new ArrayList<>();

            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String relativePath = path.relativize(file).toString();
                        allFiles.add(relativePath);

                        if (relativePath.endsWith(".java")) javaFiles.add(relativePath);
                        else if (relativePath.endsWith(".properties")
                                || relativePath.endsWith(".yml")
                                || relativePath.endsWith(".yaml")) {

                            configurationFiles.add(relativePath);
                        }
                        else resourceFiles.add(relativePath);
                    });

            return new ProjectReaderResponse(
                    true,
                    "Scanned the project: " + projectName,
                    projectName,
                    javaFiles,
                    resourceFiles,
                    configurationFiles,
                    allFiles
            );

        } catch (IOException e) {
            throw new RuntimeException("Cannot go through the directory: "+ request.getProjectPath(), e);
        }
    }

    private ProjectReaderResponse findFile(ProjectReaderRequest request) {

        try {
            Path path = getProjectPath(request);
            validate(path);

            List<String> matchingFiles = Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().equalsIgnoreCase(request.getFileName()))
                    .map(path::relativize)
                    .map(Path::toString)
                    .toList();
            return new ProjectReaderResponse(true, "Found some matching files: \n",matchingFiles);

        } catch (IOException e) {
            throw new RuntimeException("Cannot find file: "+ request.getProjectPath(), e);
        }
    }

    private ProjectReaderResponse findText(ProjectReaderRequest request) {
        try {
            Path path = getProjectPath(request);
            validate(path);

            List<String> matchingFiles = Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(file -> {
                        try {
                            return Files.readString(file)
                                    .toLowerCase().contains(request.getSearchText().toLowerCase());
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .map(path::relativize)
                    .map(Path::toString)
                    .toList();
            return new ProjectReaderResponse(true, "Found some files matching the text sent: ",matchingFiles);

        } catch (IOException e) {
            throw new RuntimeException("Cannot search text in project: " + request.getProjectPath(), e);
        }
    }

    private Path getProjectPath(ProjectReaderRequest request) {
        return Path.of(request.getProjectPath());
    }

    private void validate(Path path) {

        if (!Files.exists(path)) throw new RuntimeException("Directory not found: " + path);

        if (!Files.isDirectory(path)) throw new RuntimeException("Not a directory: " + path);

    }

}
