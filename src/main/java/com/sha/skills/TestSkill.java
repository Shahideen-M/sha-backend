package com.sha.skills;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.skills.dto.request.TestRequest;
import com.sha.skills.dto.response.TestResponse;
import com.sha.skills.enums.SkillType;
import com.sha.skills.enums.TestOperation;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestSkill implements Skill<TestRequest, TestResponse> {

    @Override
    public SkillType getType() {
        return SkillType.PROJECT;
    }

    @Override
    public TestResponse executeTyped(TestRequest request) {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mvnw.cmd",
                    "test"
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

            return new TestResponse(
                    exitCode == 0,
                    exitCode,
                    output
            );

        } catch (Exception e) {
            return new TestResponse(
                    false,
                    -1,
                    "Tests failed to start: " + e.getMessage()
            );
        }
    }

    @Override
    public Class<TestRequest> getRequestClass() {
        return TestRequest.class;
    }

    @Override
    public TestResponse execute(Object request) {
        return executeTyped((TestRequest) request);
    }

    @Override
    public SkillPrompt<?> describe() {
        return new SkillPrompt<>(
                SkillType.PROJECT,
                "Run tests for a Java Maven project.",
                List.of(
                        "run tests",
                        "test project",
                        "maven test",
                        "run project tests"
                ),
                List.of(
                        new OperationPrompt<>(
                                TestOperation.TEST,
                                "Run Maven tests.",
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