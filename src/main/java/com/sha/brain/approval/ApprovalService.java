package com.sha.brain.approval;

import com.sha.skills.Skill;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApprovalService {

    private final Map<String, PendingApproval> pendingApprovals = new ConcurrentHashMap<>();

    public String create(
            String toolName,
            String userMessage,
            Skill<?, ?> skill,
            Object request
    ) {
        String id = UUID.randomUUID().toString();
        PendingApproval approval = new PendingApproval(
                id,
                toolName,
                userMessage,
                skill,
                request
        );
        pendingApprovals.put(id, approval);
        return id;
    }

    public PendingApproval get(String id) {
        return pendingApprovals.get(id);
    }

    public ApprovedAction approve(String id) {

        PendingApproval approval = pendingApprovals.remove(id);

        if (approval == null) throw new IllegalArgumentException("Approval not found: "+ id);
        return new ApprovedAction(approval);
    }

    public boolean reject(String id) {
        return pendingApprovals.remove(id) != null;
    }
}
