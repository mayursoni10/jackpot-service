package com.sporty.jackpot.dto;

import java.math.BigDecimal;

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

    // Getters and Setters
    public String getBetId() { return betId; }
    public void setBetId(String betId) { this.betId = betId; }

    public boolean isWinner() { return isWinner; }
    public void setWinner(boolean winner) { isWinner = winner; }

    public BigDecimal getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(BigDecimal rewardAmount) { this.rewardAmount = rewardAmount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
