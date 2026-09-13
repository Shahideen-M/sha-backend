package com.sha.brain;

public record ApprovedAction(
        PendingApproval approval,
        Object result
) {
}
