package com.sporty.jackpot.util;

import com.sporty.jackpot.entity.ContributionType;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.RewardType;

import java.math.BigDecimal;

public class TestDataBuilder {

    public static Jackpot createFixedJackpot(String id, BigDecimal initialPool) {
        Jackpot jackpot = new Jackpot();
        jackpot.setJackpotId(id);
        jackpot.setInitialPool(initialPool);
        jackpot.setCurrentPool(initialPool);
        jackpot.setContributionType(ContributionType.FIXED);
        jackpot.setContributionPercentage(new BigDecimal("5"));
        jackpot.setRewardType(RewardType.FIXED);
        jackpot.setRewardChancePercentage(new BigDecimal("1"));
        return jackpot;
    }

    public static Jackpot createVariableJackpot(String id, BigDecimal initialPool) {
        Jackpot jackpot = new Jackpot();
        jackpot.setJackpotId(id);
        jackpot.setInitialPool(initialPool);
        jackpot.setCurrentPool(initialPool);
        jackpot.setContributionType(ContributionType.VARIABLE);
        jackpot.setInitialContributionPercentage(new BigDecimal("10"));
        jackpot.setContributionDecreaseRate(new BigDecimal("0.001"));
        jackpot.setRewardType(RewardType.VARIABLE);
        jackpot.setInitialRewardChancePercentage(new BigDecimal("0.1"));
        jackpot.setRewardChanceIncreaseRate(new BigDecimal("0.01"));
        jackpot.setPoolLimitFor100PercentChance(new BigDecimal("100000"));
        return jackpot;
    }
}
