package com.sha.controller;

import com.sha.dto.TradeCalculationRequest;
import com.sha.dto.TradeCalculationResponse;
import com.sha.service.ShaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://sha-ai.netlify.app"
})
public class GameController {

    private final ShaService shaService;

    public GameController(ShaService shaService) {
        this.shaService = shaService;
    }

    @PostMapping("/trade/calculate")
    public TradeCalculationResponse calculation(@RequestBody TradeCalculationRequest request) {
        return shaService.calculateTrade(request);
    }
}
