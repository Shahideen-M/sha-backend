package com.sha.brain.approval;

import com.sha.agents.tools.AgentShaTool;

public record AgentPendingApproval(
        String id,
        String toolName,
        String toolArguments,
        String agentName,
        String projectPath,
        String userMessage,
        String systemPrompt,
        String safeResults,
        AgentShaTool tool
) {
}