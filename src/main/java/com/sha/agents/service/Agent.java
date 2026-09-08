package com.sha.agents.service;

import com.sha.agents.enums.AgentType;
import com.sha.brain.prompt.AgentPrompt;

public interface Agent<REQ, RES> {

    AgentType getType();
    RES executeTyped(REQ request);
    Class<REQ> getRequestClass();
    RES execute(Object request);
    AgentPrompt<?> describe();
}