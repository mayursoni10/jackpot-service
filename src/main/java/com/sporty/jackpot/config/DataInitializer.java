package com.sporty.jackpot.config;

import com.sporty.jackpot.entity.ContributionType;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.RewardType;
import com.sporty.jackpot.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(JackpotRepository jackpotRepository) {
        return args -> {
            // Create fixed contribution and fixed reward jackpot
            Jackpot jackpot1 = new Jackpot();
            jackpot1.setJackpotId("JACKPOT-001");
            jackpot1.setInitialPool(new BigDecimal("1000"));
            jackpot1.setCurrentPool(new BigDecimal("1000"));
            jackpot1.setContributionType(ContributionType.FIXED);
            jackpot1.setRewardType(RewardType.FIXED);

            // Fixed contribution config
            jackpot1.setContributionPercentage(new BigDecimal("5")); // 5%

            // Fixed reward config
            jackpot1.setRewardChancePercentage(new BigDecimal("0.1")); // 0.1%

            jackpotRepository.save(jackpot1);
            log.info("Created fixed jackpot: {}", jackpot1);

            // Create variable contribution and variable reward jackpot
            Jackpot jackpot2 = new Jackpot();
            jackpot2.setJackpotId("JACKPOT-002");
            jackpot2.setInitialPool(new BigDecimal("5000"));
            jackpot2.setCurrentPool(new BigDecimal("5000"));
            jackpot2.setContributionType(ContributionType.VARIABLE);
            jackpot2.setRewardType(RewardType.VARIABLE);

            // Variable contribution config
            jackpot2.setInitialContributionPercentage(new BigDecimal("10")); // Start at 10%
            jackpot2.setContributionDecreaseRate(new BigDecimal("0.001")); // Decrease rate

            // Variable reward config
            jackpot2.setInitialRewardChancePercentage(new BigDecimal("0.1")); // Start at 0.1%
            jackpot2.setRewardChanceIncreaseRate(new BigDecimal("0.002")); // Increase rate
            jackpot2.setPoolLimitFor100PercentChance(new BigDecimal("50000")); // 100% win at 50k

            jackpotRepository.save(jackpot2);
            log.info("Created variable jackpot: {}", jackpot2);
        };
    }
}