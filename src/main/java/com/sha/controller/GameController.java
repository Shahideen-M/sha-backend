package com.sha.controller;

import com.sha.dto.CraftCalculationRequest;
import com.sha.dto.CraftCalculationResponse;
import com.sha.service.ShaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
public class GameController {

    private final ShaService shaService;

    public GameController(ShaService shaService) {
        this.shaService = shaService;
    }

    @PostMapping("/craft/calculate")
    public CraftCalculationResponse calculation(@RequestBody CraftCalculationRequest request) {
        return shaService.calculateCraft(request);
    }
}
