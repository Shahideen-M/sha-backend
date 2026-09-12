package com.sha.skills.tools;

import com.sha.skills.FileSkill;
import com.sha.skills.Skill;
import com.sha.skills.dto.request.FileRequest;
import com.sha.skills.dto.response.FileResponse;
import com.sha.skills.enums.FileOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Getter
@Component
@RequiredArgsConstructor
public class FileTools implements ShaTool {

    private final FileSkill fileSkill;
    private final ObjectMapper objectMapper;


    @Override
    public Skill<?, ?> getSkill() {
        return fileSkill;
    }

    @Override
    public Object createRequest(String toolName, String arguments) {

        FileRequest request = new FileRequest();
        var json = objectMapper.readTree(arguments);

        switch (toolName) {
            case "listFiles" -> {
                request.setPath(json.get("path").asString());
                request.setOperation(FileOperation.LIST);
            }
            case "deleteFile" -> {
                request.setPath(json.get("path").asString());
                request.setOperation(FileOperation.DELETE);
            }
            case "writeFile" -> {
                request.setPath(json.get("path").asString());
                request.setContent(json.get("content").asString());
                request.setOperation(FileOperation.WRITE);
            }
            default -> throw new IllegalArgumentException("Unknown FileTools operation: " + toolName);
        }
        return request;
    }

    @Tool(description = "List files and directories at the given path")
    public FileResponse listFiles(String path) {
        FileRequest request = new FileRequest();
        request.setPath(path);
        request.setOperation(FileOperation.LIST);
        return fileSkill.execute(request);
    }

    @Tool(description = "Deleting a file at the given path")
    public FileResponse deleteFile(String path) {
        FileRequest request = new FileRequest();
        request.setPath(path);
        request.setOperation(FileOperation.DELETE);
        return fileSkill.execute(request);
    }

    @Tool(description = "Write content to a file at the given path")
    public FileResponse writeFile(String path, String content) {
        FileRequest request = new FileRequest();
        request.setPath(path);
        request.setContent(content);
        request.setOperation(FileOperation.WRITE);
        return fileSkill.execute(request);
    }
}
