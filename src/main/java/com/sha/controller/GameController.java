package com.sha.controller;

import com.sha.skills.dto.request.TradeCalculationRequest;
import com.sha.skills.dto.response.TradeCalculationResponse;
import com.sha.skills.service.ShaService;
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
