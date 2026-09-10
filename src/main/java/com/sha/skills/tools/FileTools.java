package com.sha.skills.tools;

import com.sha.brain.AuthorityManager;
import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.enums.ExecutionTargetType;
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
    private final AuthorityManager authorityManager;
    private final ApprovalService approvalService;

    @Tool(description = "List files and directories at the given path")
    public FileResponse listFiles(String path) {
        FileRequest request = new FileRequest();
        request.setPath(path);
        request.setOperation(FileOperation.LIST);
        return authority(request);
    }

    @Tool(description = "Deleting a file at the given path")
    public FileResponse deleteFile(String path) {
        FileRequest request = new FileRequest();
        request.setPath(path);
        request.setOperation(FileOperation.DELETE);
        return authority(request);
    }

    @Tool(description = "Write content to a file at the given path")
    public FileResponse writeFile(String path, String content) {
        FileRequest request = new FileRequest();
        request.setPath(path);
        request.setContent(content);
        request.setOperation(FileOperation.WRITE);
        return authority(request);
    }

    private FileResponse authority(FileRequest request) {
        AuthorityLevel auth = authorityManager.check(ExecutionTargetType.SKILL, "FILE", request.getOperation());

        switch (auth) {
            case SAFE: return fileSkill.executeTyped(request);
            case APPROVAL_REQUIRED:
                String token = approvalService.requestApproval(" ");
            case BLOCKED -> new FileResponse(
                    false,
                    "Action Blocked"
            );
        };
    }
}
