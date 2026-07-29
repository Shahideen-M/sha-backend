package com.sha.controller;

import com.sha.dto.request.*;
import com.sha.dto.response.*;
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

    @PostMapping("/local/app")
    public AppLauncherResponse app(@RequestBody AppLauncherRequest request) {
        return shaService.appLauncher(request);
    }

    @PostMapping("/local/browser")
    public BrowserResponse browser(@RequestBody BrowserRequest request) {
        return shaService.browser(request);
    }
}
