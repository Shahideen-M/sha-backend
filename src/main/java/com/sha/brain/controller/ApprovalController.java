package com.sha.brain.controller;

import com.sha.brain.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/approve")
    public Object approve(@RequestParam String token) {
        return approvalService.approve(token);
    }

    @PostMapping("/reject")
    public void reject(@RequestParam String token) {
        approvalService.reject(token);
    }
}
