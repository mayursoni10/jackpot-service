package com.sporty.jackpot.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BetRequest {
    private String betId;
    private String userId;
    private String jackpotId;
    private BigDecimal betAmount;
}
