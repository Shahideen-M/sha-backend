package com.sha.skills.service.impl;

import com.sha.skills.dto.request.ChatRequest;
import com.sha.skills.dto.request.ImageAnalysisRequest;
import com.sha.skills.dto.response.ChatResponse;
import com.sha.skills.dto.response.ImageAnalysisResponse;
import org.springframework.stereotype.Service;

@Service
public class AIRouter {

    private final GeminiAIService geminiAIService;
    private final OllamaAIService ollamaAIService;

    public AIRouter(GeminiAIService geminiAIService, OllamaAIService ollamaAIService) {
        this.geminiAIService = geminiAIService;
        this.ollamaAIService = ollamaAIService;
    }

    public ChatResponse ollamaChat(ChatRequest request) {
        return ollamaAIService.chat(request);
    }

    public ChatResponse geminiChat(ChatRequest request) {
        return geminiAIService.chat(request);
    }

    public ImageAnalysisResponse geminiAnalyzeImages(ImageAnalysisRequest request) {
        return geminiAIService.analyzeImages(request);
    }
}
