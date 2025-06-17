package com.sporty.jackpot.service;

import com.sporty.jackpot.entity.ContributionType;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.JackpotContribution;
import com.sporty.jackpot.factory.ContributionStrategyFactory;
import com.sporty.jackpot.model.Bet;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.strategy.contribution.ContributionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JackpotContributionService {

    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository contributionRepository;
    private final ContributionStrategyFactory strategyFactory;

    public void processBet(Bet bet) {
        log.info("Processing bet contribution: {}", bet);

        // CHECK FOR MATCHING JACKPOT BY ID
        Optional<Jackpot> jackpotOpt = jackpotRepository.findById(bet.getJackpotId());

        // IF NO MATCHING JACKPOT, EXIT
        if (jackpotOpt.isEmpty()) {
            log.warn("Jackpot not found for ID: {}", bet.getJackpotId());
            return;
        }

        // MATCHING JACKPOT FOUND - CONTRIBUTE ACCORDING TO ITS CONFIGURATION
        Jackpot jackpot = jackpotOpt.get();
        BigDecimal contributionAmount = calculateContribution(jackpot, bet.getBetAmount());

        // Update jackpot pool
        jackpot.setCurrentPool(jackpot.getCurrentPool().add(contributionAmount));
        jackpotRepository.save(jackpot);

        // Create contribution record
        JackpotContribution contribution = new JackpotContribution();
        contribution.setBetId(bet.getBetId());
        contribution.setUserId(bet.getUserId());
        contribution.setJackpotId(jackpot.getJackpotId());
        contribution.setStakeAmount(bet.getBetAmount());
        contribution.setContributionAmount(contributionAmount);
        contribution.setCurrentJackpotAmount(jackpot.getCurrentPool());
        contribution.setCreatedAt(LocalDateTime.now());

        contributionRepository.save(contribution);

        log.info("Contribution processed: {} contributed to jackpot {}", contributionAmount, jackpot.getJackpotId());
    }

    private BigDecimal calculateContribution(Jackpot jackpot, BigDecimal betAmount) {
        ContributionStrategy strategy = strategyFactory.getStrategy(jackpot.getContributionType());
        return strategy.calculate(jackpot, betAmount);
    }
}
