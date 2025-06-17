package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.entity.Jackpot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FixedRewardStrategy implements RewardStrategy {

    @Override
    public BigDecimal calculateWinChance(Jackpot jackpot) {
        return jackpot.getRewardChancePercentage();
    }
}
