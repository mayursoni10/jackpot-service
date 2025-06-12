package com.sporty.jackpot.config;

import com.sporty.jackpot.entity.ContributionType;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.RewardType;
import com.sporty.jackpot.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JackpotRepository jackpotRepository;

    @Override
    public void run(String... args) {
        // Create a fixed contribution/reward jackpot
        Jackpot fixedJackpot = new Jackpot();
        fixedJackpot.setJackpotId("JACKPOT-001");
        fixedJackpot.setInitialPool(new BigDecimal("1000"));
        fixedJackpot.setCurrentPool(new BigDecimal("1000"));
        fixedJackpot.setContributionType(ContributionType.FIXED);
        fixedJackpot.setContributionPercentage(new BigDecimal("5"));
        fixedJackpot.setRewardType(RewardType.FIXED);
        fixedJackpot.setRewardChancePercentage(new BigDecimal("0.1"));
        jackpotRepository.save(fixedJackpot);

        // Create a variable contribution/reward jackpot
        Jackpot variableJackpot = new Jackpot();
        variableJackpot.setJackpotId("JACKPOT-002");
        variableJackpot.setInitialPool(new BigDecimal("5000"));
        variableJackpot.setCurrentPool(new BigDecimal("5000"));
        variableJackpot.setContributionType(ContributionType.VARIABLE);
        variableJackpot.setInitialContributionPercentage(new BigDecimal("10"));
        variableJackpot.setContributionDecreaseRate(new BigDecimal("0.001"));
        variableJackpot.setRewardType(RewardType.VARIABLE);
        variableJackpot.setInitialRewardChancePercentage(new BigDecimal("0.05"));
        variableJackpot.setRewardChanceIncreaseRate(new BigDecimal("0.002"));
        variableJackpot.setPoolLimitFor100PercentChance(new BigDecimal("50000"));
        jackpotRepository.save(variableJackpot);
    }
}