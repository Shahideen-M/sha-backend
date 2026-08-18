package com.sha.service.impl;

import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class AIRouter {

    private final GeminiAIService geminiAIService;
    private final OllamaAIService ollamaAIService;

    public AIRouter(GeminiAIService geminiAIService, OllamaAIService ollamaAIService) {
        this.geminiAIService = geminiAIService;
        this.ollamaAIService = ollamaAIService;
    }

    public ChatResponse chat(ChatRequest request) {
        return ollamaAIService.chat(request);
    }

//    public ChatResponse chat(ChatRequest request) {
//        return geminiAIService.chat(request);
//    }
}
