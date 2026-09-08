package com.sha.skills.tools;

import com.sha.skills.FileSkill;
import com.sha.skills.dto.request.FileRequest;
import com.sha.skills.dto.response.FileResponse;
import com.sha.skills.enums.FileOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileTools {

    private final FileSkill fileSkill;

    @Tool(description = "List files and directories at the given path")
    public FileResponse listFiles(String path) {
        FileRequest request = new FileRequest();
        request.setPath(path);
        request.setOperation(FileOperation.LIST);
        return fileSkill.executeTyped(request);
    }
}
