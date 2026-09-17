package com.sha.skills.tools;

import com.sha.skills.AppLauncherSkill;
import com.sha.skills.Skill;
import com.sha.skills.dto.request.AppLauncherRequest;
import com.sha.skills.dto.response.AppLauncherResponse;
import com.sha.skills.enums.LaunchOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Getter
@Component
@RequiredArgsConstructor
public class AppLauncherTools implements ShaTool{

    private final AppLauncherSkill appLauncherSkill;
    private final ObjectMapper objectMapper;

    @Override
    public Skill<?, ?> getSkill() {
        return appLauncherSkill;
    }

    @Override
    public Object createRequest(String toolName, String arguments) {

        AppLauncherRequest request = new AppLauncherRequest();
        var json = objectMapper.readTree(arguments);

        switch (toolName) {
            case "openApplication" -> {
                request.setApplicationName(json.get("applicationName").asString());
                request.setOperation(LaunchOperation.OPEN_APPLICATION);
            }
            case "openFile" -> {
                request.setPath(json.get("path").asString());
                request.setOperation(LaunchOperation.OPEN_FILE);
            }
            case "openFolder" -> {
                request.setPath(json.get("path").asString());
                request.setOperation(LaunchOperation.OPEN_FOLDER);
            }
            default -> throw new IllegalArgumentException("Unknown AppLauncherTools operation: " + toolName);
        }
        return request;
    }

    @Tool(description = "Open a supported desktop application")
    public AppLauncherResponse openApplication(String applicationName) {
        AppLauncherRequest request = new AppLauncherRequest();
        request.setApplicationName(applicationName);
        request.setOperation(LaunchOperation.OPEN_APPLICATION);
        return appLauncherSkill.execute(request);
    }

    @Tool(description = "Open an existing file")
    public AppLauncherResponse openFile(String path) {
        AppLauncherRequest request = new AppLauncherRequest();
        request.setPath(path);
        request.setOperation(LaunchOperation.OPEN_FILE);
        return appLauncherSkill.execute(request);
    }

    @Tool(description = "Open an existing folder")
    public AppLauncherResponse openFolder(String path) {
        AppLauncherRequest request = new AppLauncherRequest();
        request.setPath(path);
        request.setOperation(LaunchOperation.OPEN_FOLDER);
        return appLauncherSkill.execute(request);
    }
}