package com.sha.agents.tools;

import com.sha.brain.enums.AuthorityLevel;
import org.springframework.ai.tool.ToolCallback;

public interface AgentShaTool {

    ToolCallback getTool();
    AuthorityLevel getAuthority();
}