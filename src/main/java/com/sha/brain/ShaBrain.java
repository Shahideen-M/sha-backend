package com.sha.brain;

import com.sha.brain.approval.ApprovalService;
import com.sha.brain.approval.ApprovedAction;
import com.sha.brain.dto.ShaBrainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShaBrain {

    private final ToolExecutionService toolExecutionService;
    private final ApprovalService approvalService;

    public ShaBrainResponse process(String userMessage) {
        return toolExecutionService.execute(userMessage);
    }

    public ShaBrainResponse approve(String approvalId) {
        ApprovedAction approvedAction = approvalService.approve(approvalId);
        return toolExecutionService.resume(approvedAction);
    }

    public boolean reject(String approvalId) {
        return approvalService.reject(approvalId);
    }

}