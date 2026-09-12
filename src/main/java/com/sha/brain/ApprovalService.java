package com.sha.brain;

import com.sha.skills.Skill;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApprovalService {

    private final Map<String, PendingApproval> pendingApprovals = new ConcurrentHashMap<>();

    public String create(Skill<?, ?> skill, Object request) {
        String id = UUID.randomUUID().toString();

        pendingApprovals.put(id, new PendingApproval(id, skill, request));
        return id;
    }

    public PendingApproval get(String id) {
        return pendingApprovals.get(id);
    }

    public Object approve(String id) {

        PendingApproval approval = pendingApprovals.remove(id);

        if (approval == null) throw new IllegalArgumentException("Approval not found: "+ id);

        return approval.skill().execute(approval.request());
    }

    public boolean reject(String id) {
        return pendingApprovals.remove(id) != null;
    }
}
