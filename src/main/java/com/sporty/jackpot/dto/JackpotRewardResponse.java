package com.sporty.jackpot.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class JackpotRewardResponse {
    private boolean won;
    private BigDecimal rewardAmount;
    private String message;
}
