package com.sha.skills.tools;

import com.sha.skills.Skill;
import com.sha.skills.TestSkill;
import com.sha.skills.dto.request.TestRequest;
import com.sha.skills.dto.response.TestResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Getter
@Component
@RequiredArgsConstructor
public class TestTools implements ShaTool {

    private final TestSkill testSkill;
    private final ObjectMapper objectMapper;

    @Override
    public Skill<?, ?> getSkill() {
        return testSkill;
    }

    @Override
    public Object createRequest(String toolName, String arguments) {

        var json = objectMapper.readTree(arguments);

        if (!toolName.equals("runTests")) {
            throw new IllegalArgumentException("Unknown TestTools operation: " + toolName);
        }

        TestRequest request = new TestRequest();
        request.setProjectPath(json.get("projectPath").asString());

        return request;
    }

    @Tool(description = "Run all tests in a Java Maven project and return the test output")
    public TestResponse runTests(String projectPath) {
        TestRequest request = new TestRequest();
        request.setProjectPath(projectPath);
        return testSkill.execute(request);
    }
}