package com.sha.service.skills;

import com.sha.dto.request.ProjectReaderRequest;
import com.sha.dto.response.ProjectReaderResponse;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectReaderSkill implements Skill<ProjectReaderRequest, ProjectReaderResponse> {

    @Override
    public SkillType getType() {
        return SkillType.PROJECT;
    }

    @Override
    public ProjectReaderResponse execute(ProjectReaderRequest request) {
        return scanProject(request);
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

    private Path getProjectPath(ProjectReaderRequest request) {
        return Path.of(request.getProjectPath());
    }

    private void validate(Path path) {

        if (!Files.exists(path)) throw new RuntimeException("Directory not found: " + path);

        if (!Files.isDirectory(path)) throw new RuntimeException("Not a directory: " + path);

    }

}
