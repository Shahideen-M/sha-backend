package com.sha.skills;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.skills.dto.request.BuildRequest;
import com.sha.skills.dto.response.BuildResponse;
import com.sha.skills.enums.BuildOperation;
import com.sha.skills.enums.SkillType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildSkill implements Skill<BuildRequest, BuildResponse> {

    @Override
    public SkillType getType() {
        return SkillType.PROJECT;
    }

    @Override
    public BuildResponse executeTyped(BuildRequest request) {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mvnw.cmd",
                    "clean",
                    "compile"
            );

            processBuilder
                    .directory(new java.io.File(request.getProjectPath()))
                    .redirectErrorStream(true);

            Process process = processBuilder.start();

            String output;

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(process.getInputStream())
                         )) {

                output = reader.lines()
                        .collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();

            return new BuildResponse(
                    exitCode == 0,
                    exitCode,
                    output
            );

        } catch (Exception e) {
            return new BuildResponse(
                    false,
                    -1,
                    "Build failed to start: " + e.getMessage()
            );
        }
    }

    @Override
    public Class<BuildRequest> getRequestClass() {
        return BuildRequest.class;
    }

    @Override
    public BuildResponse execute(Object request) {
        return executeTyped((BuildRequest) request);
    }

    @Override
    public SkillPrompt<?> describe() {
        return new SkillPrompt<>(
                SkillType.PROJECT,
                "Build a Java Maven project.",
                List.of(
                        "build project",
                        "compile project",
                        "maven build",
                        "compile code"
                ),
                List.of(
                        new OperationPrompt<>(
                                BuildOperation.BUILD,
                                "Compile a Maven project.",
                                List.of("projectPath"),
                                """
                                {
                                  "projectPath":"D:\\Projects\\Sha"
                                }
                                """
                        )
                )
        );
    }

    @Override
    public AuthorityLevel getAuthority(Object request) {
        return AuthorityLevel.APPROVAL_REQUIRED;
    }
}