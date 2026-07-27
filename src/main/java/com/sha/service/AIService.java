package com.sha.service;

import com.sha.dto.request.ChatRequest;
import com.sha.dto.response.ChatResponse;

public interface AIService {

    ChatResponse chat(ChatRequest chatRequest);
}
