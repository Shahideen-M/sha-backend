package com.sha.brain;

import com.sha.brain.approval.AgentApprovalService;
import com.sha.brain.approval.AgentPendingApproval;
import com.sha.brain.approval.ApprovalService;
import com.sha.brain.approval.ApprovedAction;
import com.sha.brain.approval.ApprovedAgentAction;
import com.sha.brain.approval.PendingApproval;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.ShaResponseType;
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

    public ShaBrainResponse reject(String approvalId) {
        PendingApproval skillApproval = approvalService.rejectAndGet(approvalId);
        if (skillApproval != null) {
            return rejected(skillApproval.toolName());
        }
        AgentPendingApproval agentApproval = agentApprovalService.rejectAndGet(approvalId);
        if (agentApproval != null) {
            return rejected(agentApproval.toolName());
        }
        return new ShaBrainResponse(
                ShaResponseType.ERROR,
                "No pending approval found for this request. "
                        + "It may have already been approved, rejected, or expired.",
                null,
                false,
                ""
        );
    }

    private ShaBrainResponse rejected(String toolName) {
        return new ShaBrainResponse(
                ShaResponseType.REJECTED,
                "Rejected " + toolName
                        + ". The action was not executed and the current execution has stopped.",
                null,
                false,
                ""
        );
    }
}