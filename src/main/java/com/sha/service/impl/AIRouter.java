package com.sha.service.impl;

import com.sha.dto.request.ChatRequest;
import com.sha.dto.request.ImageAnalysisRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.dto.response.ImageAnalysisResponse;
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
