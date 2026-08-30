package com.sha.brain;

import com.sha.brain.dto.PendingExecution;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApprovalStore {

    private final Map<String, PendingExecution> pendingExecutions = new ConcurrentHashMap<>();

    public String create(PendingExecution execution) {
        String token = UUID.randomUUID().toString();
        pendingExecutions.put(token, execution);
        return token;
    }

    public PendingExecution consume(String token) {
        return pendingExecutions.remove(token);
    }

    public void remove(String token) {
        pendingExecutions.remove(token);
    }
}
