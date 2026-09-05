package com.sha.service.impl;

import com.sha.dto.request.ChatRequest;
import com.sha.dto.request.ImageAnalysisRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.dto.response.ImageAnalysisResponse;
import com.sha.service.AIService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
public class GeminiAIService implements AIService {

    private final ChatClient chatClient;

    public GeminiAIService(@Qualifier("geminiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Value("${sha.ai.system-prompt}")
    private String systemPrompt;

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        String aiResponse =  chatClient.prompt()
                .system(systemPrompt)
                .user(chatRequest.getMessage())
                .advisors(advisor ->
                        advisor.param(ChatMemory.CONVERSATION_ID, "default")
                )
                .call()
                .content();
        return new ChatResponse(aiResponse);
    }

    public ImageAnalysisResponse analyzeImages(ImageAnalysisRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().isEmpty()) {
            throw new IllegalArgumentException("Image analysis prompt is required.");
        }
        if (request.getImagePaths() == null || request.getImagePaths().isEmpty()) {
            throw new IllegalArgumentException("At least one image is required.");
        }
        String aiResponse = chatClient.prompt()
                .system("""
                        You are Sha's image analysis system.
                        Analyze the provided images carefully.

                        Only describe information you can actually
                        determine from the images.

                        Do not invent details.

                        Follow the user's analysis request exactly.
                        """)
                .advisors(advisor ->
                        advisor.param(ChatMemory.CONVERSATION_ID, "image-analysis")
                )
                .user(userSpec -> {
                    userSpec.text(request.getPrompt());
                    for (String imagePath : request.getImagePaths()) {
                        if (imagePath == null || imagePath.isBlank()) continue;
                        userSpec.media(MimeTypeUtils.IMAGE_PNG,
                                new FileSystemResource(imagePath));
                    }
                })
                .call()
                .content();
        return new ImageAnalysisResponse(aiResponse);
    }
}