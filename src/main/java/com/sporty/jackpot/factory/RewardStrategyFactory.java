package com.sporty.jackpot.factory;

import com.sporty.jackpot.entity.RewardType;
import com.sporty.jackpot.strategy.reward.FixedRewardStrategy;
import com.sporty.jackpot.strategy.reward.RewardStrategy;
import com.sporty.jackpot.strategy.reward.VariableRewardStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RewardStrategyFactory {

    @Autowired
    private FixedRewardStrategy fixedStrategy;

    @Autowired
    private VariableRewardStrategy variableStrategy;

    private Map<RewardType, RewardStrategy> strategies;

    @PostConstruct
    public void init() {
        strategies = new HashMap<>();
        strategies.put(RewardType.FIXED, fixedStrategy);
        strategies.put(RewardType.VARIABLE, variableStrategy);
    }

    public RewardStrategy getStrategy(RewardType type) {
        RewardStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for reward type: " + type);
        }
        return strategy;
    }

    // Method to register new strategies dynamically
    public void registerStrategy(RewardType type, RewardStrategy strategy) {
        strategies.put(type, strategy);
    }
}
