package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.entity.Jackpot;

import java.math.BigDecimal;

public interface RewardStrategy {
    /**
     * Calculate the win chance percentage (0-100)
     * @param jackpot The jackpot configuration
     * @return Win chance as a percentage
     */
    BigDecimal calculateWinChance(Jackpot jackpot);
}
