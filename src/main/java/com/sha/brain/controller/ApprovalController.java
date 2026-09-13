package com.sha.brain.controller;

import com.sha.brain.ApprovalService;
import com.sha.brain.PendingApproval;
import com.sha.brain.ShaBrain;
import com.sha.skills.FileSkill;
import com.sha.skills.dto.request.FileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ShaBrain shaBrain;

    @PostMapping("/approve/{id}")
    public Object approve(@PathVariable String id) {
        return shaBrain.approve(id);
    }

    @PostMapping("/reject/{id}")
    public Object reject(@PathVariable String id) {
        return shaBrain.reject(id);
    }
}
