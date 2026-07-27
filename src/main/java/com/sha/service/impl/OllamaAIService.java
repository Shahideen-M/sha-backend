package com.sha.service.impl;

import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.service.AIService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OllamaAIService implements AIService {

    private final ChatClient chatClient;

    public OllamaAIService(@Qualifier("ollamaChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Value("${sha.ai.system-prompt}")
    private String systemPrompt;

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(chatRequest.getMessage())
                .advisors(advisor ->
                        advisor.param(ChatMemory.CONVERSATION_ID, "default")
                )
                .call()
                .content();
        return new ChatResponse(aiResponse);
    }
}
