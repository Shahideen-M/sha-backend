package com.sha.brain;

import com.sha.brain.dto.PendingExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalStore approvalStore;
    private final ExecutionPlanExecutor executor;

    public Object approve(String token) {

        PendingExecution pendingExecution = approvalStore.consume(token);

        if (pendingExecution == null) throw new RuntimeException("Invalid or expired approval token.");

        return executor.resume(pendingExecution);
    }

    public void reject(String token) {
        approvalStore.remove(token);
    }
}
