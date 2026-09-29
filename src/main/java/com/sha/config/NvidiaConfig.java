package com.sha.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NvidiaConfig {

    @Value("${sha.ai.nvidia.base-url}")
    private String baseUrl;

    @Value("${sha.ai.nvidia.api-key}")
    private String apiKey;

    @Value("${sha.ai.nvidia.model}")
    private String model;

    @Bean
    public OpenAiChatModel nvidiaChatModel() {
        return OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .model(model)
                        .build())
                .build();
    }

    @Bean("nvidiaChatClient")
    public ChatClient nvidiaChatClient(
            @Qualifier("nvidiaChatModel") OpenAiChatModel nvidiaChatModel,
            ChatMemory chatMemory) {
        return ChatClient.builder(nvidiaChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
