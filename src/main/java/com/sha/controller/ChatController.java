package com.sha.controller;

import com.sha.brain.ShaBrain;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.dto.request.*;
import com.sha.dto.response.*;
import com.sha.agentsData.agents.contentcreator.dto.ContentCreatorRequest;
import com.sha.agentsData.agents.contentcreator.dto.ContentCreatorResponse;
import com.sha.agentsData.agents.contentcreator.dto.VideoEditorRequest;
import com.sha.agentsData.agents.contentcreator.dto.VideoEditorResponse;

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
    private final ShaBrain shaBrain;

    public ChatController(ShaService shaService, ShaBrain shaBrain) {
        this.shaService = shaService;
        this.shaBrain = shaBrain;
    }

    @PostMapping("/chat")
    public ShaBrainResponse message(@RequestBody ChatRequest request) {
        return shaBrain.process(request.getMessage());
    }

    @PostMapping("/local/dev")
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

    @PostMapping("/local/memory")
    public MemoryResponse memory(@RequestBody MemoryRequest request) {
        return shaService.memory(request);
    }


    @PostMapping("/local/islam")
    public IslamicResponse surah(@RequestBody IslamicRequest request) {
        return shaService.islam(request);
    }

    @PostMapping("/local/content")
    public ContentCreatorResponse content(@RequestBody ContentCreatorRequest request) {
        return shaService.content(request);
    }

    @PostMapping("/local/videoEditor")
    public VideoEditorResponse videoEditor(@RequestBody VideoEditorRequest request) {
        return shaService.videoEditor(request);
    }

    @PostMapping("/local/career")
    public CareerResponse career(@RequestBody CareerRequest request) {
        return shaService.career(request);
    }
}
