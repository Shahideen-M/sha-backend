package com.sha.brain.approval;

import com.sha.agents.tools.AgentShaTool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentApprovalService {

    private final Map<String, AgentPendingApproval> pendingApprovals =
            new ConcurrentHashMap<>();

    public String create(
            String agentName,
            String projectPath,
            String toolName,
            String toolArguments,
            String userMessage,
            String systemPrompt,
            String gatedToolCallId,
            List<AgentPendingApproval.AnsweredToolCall> answeredCalls,
            AgentShaTool tool
    ) {
        String id = UUID.randomUUID().toString();
        pendingApprovals.put(
                id,
                new AgentPendingApproval(
                        id,
                        toolName,
                        toolArguments,
                        agentName,
                        projectPath,
                        userMessage,
                        systemPrompt,
                        gatedToolCallId,
                        answeredCalls == null ? List.of() : List.copyOf(answeredCalls),
                        tool
                )
        );
        return id;
    }

    public AgentPendingApproval get(String id) {
        return pendingApprovals.get(id);
    }

    public ApprovedAgentAction approve(String id) {
        AgentPendingApproval approval = pendingApprovals.remove(id);

        if (approval == null) throw new IllegalArgumentException("Agent approval not found: " + id);
        return new ApprovedAgentAction(approval);
    }

    public boolean reject(String id) {
        return pendingApprovals.remove(id) != null;
    }
}