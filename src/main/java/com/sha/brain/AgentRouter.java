package com.sha.brain;

import com.sha.agentsData.enums.AgentType;
import com.sha.agentsData.service.Agent;
import com.sha.agentsData.service.AgentRegistry;
import com.sha.brain.prompt.AgentPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AgentRouter {

    private final AgentRegistry registry;

    public List<AgentType> findPossibleAgents(String userMessage) {

        String message = userMessage.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();

        Set<AgentType> possibleAgents = new LinkedHashSet<>();

        for (Agent<?, ?> agent : registry.getAllAgents()) {

            AgentPrompt<?> prompt = agent.describe();

            if (prompt == null || prompt.getKeywords() == null) continue;

            for (String keyword : prompt.getKeywords()) {

                String normalizedKeyword = keyword.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();

                if (message.contains(normalizedKeyword)) {
                    possibleAgents.add(agent.getType());
                    break;
                }
            }
        }

        if (possibleAgents.isEmpty()) {
            for (Agent<?, ?> agent : registry.getAllAgents()) {
                possibleAgents.add(agent.getType());
            }
        }
        return new ArrayList<>(possibleAgents);
    }
}