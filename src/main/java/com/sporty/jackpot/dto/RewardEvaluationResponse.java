package com.sporty.jackpot.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RewardEvaluationResponse {
    private String betId;
    private boolean isWinner;
    private BigDecimal rewardAmount;
    private String message;

    public RewardEvaluationResponse() {}

    public RewardEvaluationResponse(String betId, boolean isWinner, BigDecimal rewardAmount, String message) {
        this.betId = betId;
        this.isWinner = isWinner;
        this.rewardAmount = rewardAmount;
        this.message = message;
    }

    public RewardEvaluationResponse(boolean isWinner, BigDecimal rewardAmount, String message) {
        this.isWinner = isWinner;
        this.rewardAmount = rewardAmount;
        this.message = message;
    }
}
