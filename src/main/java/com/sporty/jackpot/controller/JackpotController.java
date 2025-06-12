package com.sporty.jackpot.controller;


import com.sporty.jackpot.dto.BetRequest;
import com.sporty.jackpot.dto.JackpotRewardResponse;
import com.sporty.jackpot.service.BetService;
import com.sporty.jackpot.service.JackpotRewardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JackpotController {
    private static final Logger logger = LoggerFactory.getLogger(JackpotController.class);

    private final BetService betService;
    private final JackpotRewardService rewardService;

    @PostMapping("/bets")
    public ResponseEntity<String> publishBet(@RequestBody BetRequest betRequest) {
        betService.publishBet(betRequest);
        return ResponseEntity.ok("Bet published successfully");
    }

    @GetMapping("/bets/{betId}/evaluate")
    public ResponseEntity<JackpotRewardResponse> evaluateJackpotReward(@PathVariable String betId) {
        JackpotRewardResponse response = rewardService.evaluateJackpotReward(betId);
        return ResponseEntity.ok(response);
    }
}
