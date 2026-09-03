package com.sha.agentsData.service;

import com.sha.agentsData.enums.AgentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentRegistry {

    private final List<Agent<?, ?>> agents;

    public <T extends Agent<?, ?>> T findAgent(AgentType type, Class<T> agentClass) {

        Agent<?, ?> agent = agents.stream()
                .filter(a -> a.getType() == type)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        return agentClass.cast(agent);
    }

    public List<Agent<?, ?>> getAllAgents() {
        return agents;
    }
}