package com.sha.brain;

import com.sha.brain.dto.PendingAction;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApprovalStore {

    private final Map<String, PendingAction> pendingActions = new ConcurrentHashMap<>();

    public String create(PendingAction action) {
        String token = UUID.randomUUID().toString();
        pendingActions.put(token, action);
        return token;
    }

    public PendingAction consume(String token) {
        return pendingActions.remove(token);
    }

    public void remove(String token) {
        pendingActions.remove(token);
    }
}
