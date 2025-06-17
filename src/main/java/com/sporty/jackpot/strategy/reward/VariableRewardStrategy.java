package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.entity.Jackpot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class VariableRewardStrategy implements RewardStrategy {

    @Override
    public BigDecimal calculateWinChance(Jackpot jackpot) {
        BigDecimal currentPool = jackpot.getCurrentPool();
        BigDecimal poolLimit = jackpot.getPoolLimitFor100PercentChance();

        // If pool hits limit, 100% win chance
        if (currentPool.compareTo(poolLimit) >= 0) {
            return new BigDecimal("100");
        }

        // Calculate how much the pool has grown from initial
        BigDecimal poolGrowth = currentPool.subtract(jackpot.getInitialPool());

        // Calculate additional chance based on pool growth and increase rate
        BigDecimal additionalChance = poolGrowth.multiply(jackpot.getRewardChanceIncreaseRate())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Start with initial chance and add the calculated increase
        BigDecimal currentChance = jackpot.getInitialRewardChancePercentage().add(additionalChance);

        // Ensure we don't exceed 100%
        return currentChance.min(new BigDecimal("100"));
    }
}
