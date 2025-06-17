package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.entity.Jackpot;

import java.math.BigDecimal;

public interface ContributionStrategy {
    BigDecimal calculate(Jackpot jackpot, BigDecimal betAmount);
}
