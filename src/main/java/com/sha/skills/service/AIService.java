package com.sha.skills.service;

import com.sha.skills.dto.request.ChatRequest;
import com.sha.skills.dto.response.ChatResponse;

public interface AIService {

    ChatResponse chat(ChatRequest chatRequest);
}
