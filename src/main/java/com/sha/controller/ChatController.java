package com.sha.controller;

import com.sha.dto.request.ChatRequest;
import com.sha.dto.request.DeveloperAssistantRequest;
import com.sha.dto.request.FileRequest;
import com.sha.dto.request.ProjectReaderRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.dto.response.DeveloperAssistantResponse;
import com.sha.dto.response.FileResponse;
import com.sha.dto.response.ProjectReaderResponse;
import com.sha.service.ShaService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://sha-ai.netlify.app"
})
public class ChatController {

    private final ShaService shaService;

    public ChatController(ShaService shaService) {
        this.shaService = shaService;
    }

    @PostMapping("/chat")
    public ChatResponse message(@RequestBody ChatRequest request) {
        return shaService.chat(request);
    }

    @PostMapping("/local/chat")
    public DeveloperAssistantResponse devChat(@RequestBody DeveloperAssistantRequest request) {
        return shaService.devAssistance(request);
    }

    @PostMapping("/local/file")
    public FileResponse file(@RequestBody FileRequest request) {
        return shaService.fileOperation(request);
    }

    @PostMapping("/local/project")
    public ProjectReaderResponse project(@RequestBody ProjectReaderRequest request) {
        return shaService.projectOperation(request);
    }
}
