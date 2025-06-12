package com.sporty.jackpot.model;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.math.BigDecimal;

public class Bet {
    @NotNull
    private String betId;

    @NotNull
    private String userId;

    @NotNull
    private String jackpotId;

    @NotNull
    @Positive
    private BigDecimal betAmount;

    public Bet() {}

    public Bet(String betId, String userId, String jackpotId, BigDecimal betAmount) {
        this.betId = betId;
        this.userId = userId;
        this.jackpotId = jackpotId;
        this.betAmount = betAmount;
    }

    // Getters and Setters
    public String getBetId() { return betId; }
    public void setBetId(String betId) { this.betId = betId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getJackpotId() { return jackpotId; }
    public void setJackpotId(String jackpotId) { this.jackpotId = jackpotId; }

    public BigDecimal getBetAmount() { return betAmount; }
    public void setBetAmount(BigDecimal betAmount) { this.betAmount = betAmount; }
}
