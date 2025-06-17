package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.entity.Jackpot;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class VariableContributionStrategy implements ContributionStrategy{
    private static final BigDecimal MINIMUM_PERCENTAGE = BigDecimal.ONE; // 1%

    @Override
    public BigDecimal calculate(Jackpot jackpot, BigDecimal betAmount) {
        // Calculate how much the pool has grown from initial value
        BigDecimal poolIncrease = jackpot.getCurrentPool().subtract(jackpot.getInitialPool());
        // Calculate decrease amount based on pool growth and decrease rate
        BigDecimal decreaseAmount = poolIncrease.multiply(jackpot.getContributionDecreaseRate())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        // Start with initial percentage and subtract the decrease
        BigDecimal currentPercentage = jackpot.getInitialContributionPercentage().subtract(decreaseAmount);
        // Ensure minimum 1% contribution
        currentPercentage = currentPercentage.max(MINIMUM_PERCENTAGE);

        return betAmount.multiply(currentPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
