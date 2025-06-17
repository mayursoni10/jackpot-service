package com.sporty.jackpot.factory;

import com.sporty.jackpot.entity.ContributionType;
import com.sporty.jackpot.strategy.contribution.ContributionStrategy;
import com.sporty.jackpot.strategy.contribution.FixedContributionStrategy;
import com.sporty.jackpot.strategy.contribution.VariableContributionStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ContributionStrategyFactory {
    private FixedContributionStrategy fixedStrategy;
    private VariableContributionStrategy variableStrategy;

    private Map<ContributionType, ContributionStrategy> strategies;

    @PostConstruct
    public void init() {
        strategies = new HashMap<>();
        strategies.put(ContributionType.FIXED, fixedStrategy);
        strategies.put(ContributionType.VARIABLE, variableStrategy);
    }

    public ContributionStrategy getStrategy(ContributionType type) {
        ContributionStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for contribution type: " + type);
        }
        return strategy;
    }

    // Method to register new strategies dynamically
    public void registerStrategy(ContributionType type, ContributionStrategy strategy) {
        strategies.put(type, strategy);
    }
}
