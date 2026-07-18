package com.sha.service;

import com.sha.dto.ChatRequest;
import com.sha.dto.ChatResponse;

public interface AIService {

    ChatResponse chat(ChatRequest chatRequest);
}
