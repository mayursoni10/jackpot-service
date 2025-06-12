package com.sporty.jackpot.service;

import com.sporty.jackpot.entity.ContributionType;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.JackpotContribution;
import com.sporty.jackpot.model.Bet;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class JackpotContributionService {
    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository contributionRepository;

    @KafkaListener(topics = "jackpot-bets", groupId = "jackpot-service")
    @Transactional
    public void processBet(Bet bet) {
        log.info("Processing bet from Kafka: {}", bet);

        jackpotRepository.findById(bet.getJackpotId()).ifPresent(jackpot -> {
            BigDecimal contributionAmount = calculateContribution(jackpot, bet.getBetAmount());

            // Update jackpot pool
            jackpot.setCurrentPool(jackpot.getCurrentPool().add(contributionAmount));
            jackpotRepository.save(jackpot);

            // Create contribution record
            JackpotContribution contribution = new JackpotContribution();
            contribution.setBetId(bet.getBetId());
            contribution.setUserId(bet.getUserId());
            contribution.setJackpotId(bet.getJackpotId());
            contribution.setStakeAmount(bet.getBetAmount());
            contribution.setContributionAmount(contributionAmount);
            contribution.setCurrentJackpotAmount(jackpot.getCurrentPool());

            contributionRepository.save(contribution);
            log.info("Contribution saved: {}", contribution);
        });
    }

    private BigDecimal calculateContribution(Jackpot jackpot, BigDecimal betAmount) {
        BigDecimal percentage;

        if (jackpot.getContributionType() == ContributionType.FIXED) {
            percentage = jackpot.getContributionPercentage();
        } else {
            // Variable contribution - decreases as pool increases
            BigDecimal poolIncrease = jackpot.getCurrentPool().subtract(jackpot.getInitialPool());
            BigDecimal decreaseAmount = poolIncrease.multiply(jackpot.getContributionDecreaseRate())
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            percentage = jackpot.getInitialContributionPercentage().subtract(decreaseAmount);
            percentage = percentage.max(BigDecimal.ONE); // Minimum 1%
        }

        return betAmount.multiply(percentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
