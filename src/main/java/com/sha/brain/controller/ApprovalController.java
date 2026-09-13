package com.sha.brain.controller;

import com.sha.brain.ShaBrain;
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
