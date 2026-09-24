package com.sha.brain;

import com.sha.brain.approval.AgentApprovalService;
import com.sha.brain.approval.ApprovalService;
import com.sha.brain.approval.ApprovedAction;
import com.sha.brain.approval.ApprovedAgentAction;
import com.sha.brain.dto.ShaBrainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShaBrain {

    private final ToolExecutionService toolExecutionService;
    private final ApprovalService approvalService;
    private final AgentExecutionService agentExecutionService;
    private final AgentApprovalService agentApprovalService;

    public ShaBrainResponse process(String userMessage) {
        return toolExecutionService.execute(userMessage);
    }

    public ShaBrainResponse approve(String approvalId) {
        try {
            ApprovedAction approvedAction = approvalService.approve(approvalId);
            return toolExecutionService.resume(approvedAction);
        } catch (IllegalArgumentException e) {
            ApprovedAgentAction agentAction = agentApprovalService.approve(approvalId);
            return agentExecutionService.resume(agentAction);
        }
    }

    public boolean reject(String approvalId) {
        if (approvalService.reject(approvalId)) {
            return true;
        }
        return agentApprovalService.reject(approvalId);
    }

}