package com.sha.agentsData.service;

import com.sha.agentsData.enums.AgentType;
import com.sha.brain.prompt.AgentPrompt;

public interface Agent<REQ, RES> {

    AgentType getType();
    RES executeTyped(REQ request);
    Class<REQ> getRequestClass();
    RES execute(Object request);
    AgentPrompt<?> describe();
}