package com.sha.controller;

import com.sha.dto.ChatRequest;
import com.sha.dto.ChatResponse;
import com.sha.dto.CraftCalculationRequest;
import com.sha.dto.CraftCalculationResponse;
import com.sha.service.AIService;
import com.sha.service.ShaService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
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
}
