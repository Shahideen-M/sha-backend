package com.sha.skills.tools;

import com.sha.skills.BuildSkill;
import com.sha.skills.Skill;
import com.sha.skills.dto.request.BuildRequest;
import com.sha.skills.dto.response.BuildResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Getter
@Component
@RequiredArgsConstructor
public class BuildTools implements ShaTool {

    private final BuildSkill buildSkill;
    private final ObjectMapper objectMapper;

    @Override
    public Skill<?, ?> getSkill() {
        return buildSkill;
    }

    @Override
    public Object createRequest(String toolName, String arguments) {

        var json = objectMapper.readTree(arguments);

        if (!toolName.equals("buildProject")) {
            throw new IllegalArgumentException("Unknown BuildTools operation: " + toolName);
        }

        BuildRequest request = new BuildRequest();
        request.setProjectPath(json.get("projectPath").asString());

        return request;
    }

    @Tool(description = "Build a Java Maven project and return the build output")
    public BuildResponse buildProject(String projectPath) {
        BuildRequest request = new BuildRequest();
        request.setProjectPath(projectPath);
        return buildSkill.execute(request);
    }
}