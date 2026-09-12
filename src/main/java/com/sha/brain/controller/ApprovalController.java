package com.sha.brain.controller;

import com.sha.brain.ApprovalService;
import com.sha.brain.PendingApproval;
import com.sha.skills.FileSkill;
import com.sha.skills.dto.request.FileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/approve/{id}")
    public Object approve(@PathVariable String id) {
        return approvalService.approve(id);
    }

    @PostMapping("/reject/{id}")
    public Object reject(@PathVariable String id) {
        return approvalService.reject(id);
    }
}
