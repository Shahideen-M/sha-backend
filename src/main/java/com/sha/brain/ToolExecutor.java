package com.sha.brain;

import com.sha.skills.tools.FileTools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ToolExecutor {

    private final FileTools fileTools;

    public Object execute(String toolName, Map<String, Object> arguments) {
        return switch (toolName) {
            case "listFiles" -> fileTools.listFiles((String) arguments.get("path"));
            case "deleteFiles" -> fileTools.deleteFile((String) arguments.get("path"));
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }
}
