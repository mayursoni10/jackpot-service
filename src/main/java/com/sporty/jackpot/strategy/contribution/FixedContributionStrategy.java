package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.entity.Jackpot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FixedContributionStrategy implements ContributionStrategy{
    @Override
    public BigDecimal calculate(Jackpot jackpot, BigDecimal betAmount) {
        BigDecimal percentage = jackpot.getContributionPercentage();
        return betAmount.multiply(percentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
