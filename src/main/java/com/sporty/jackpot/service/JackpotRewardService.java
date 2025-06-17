package com.sporty.jackpot.service;

import com.sporty.jackpot.dto.RewardEvaluationResponse;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.JackpotContribution;
import com.sporty.jackpot.entity.JackpotReward;
import com.sporty.jackpot.factory.RewardStrategyFactory;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import com.sporty.jackpot.strategy.reward.RewardStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JackpotRewardService {

    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository contributionRepository;
    private final JackpotRewardRepository rewardRepository;
    private final RewardStrategyFactory strategyFactory;

    public RewardEvaluationResponse evaluateReward(String betId) {
        log.info("Evaluating reward for bet: {}", betId);

        // Check if bet has already been evaluated
        Optional<JackpotReward> existingReward = rewardRepository.findByBetId(betId);
        if (existingReward.isPresent()) {
            JackpotReward reward = existingReward.get();
            return new RewardEvaluationResponse(
                    true,
                    reward.getJackpotRewardAmount(),
                    "Jackpot already awarded for this bet!"
            );
        }

        // Find contribution for this bet
        Optional<JackpotContribution> contributionOpt = contributionRepository.findByBetId(betId);
        if (contributionOpt.isEmpty()) {
            return new RewardEvaluationResponse(
                    false,
                    BigDecimal.ZERO,
                    "Bet not found or not eligible for jackpot"
            );
        }

        JackpotContribution contribution = contributionOpt.get();
        Optional<Jackpot> jackpotOpt = jackpotRepository.findById(contribution.getJackpotId());

        if (jackpotOpt.isEmpty()) {
            return new RewardEvaluationResponse(
                    false,
                    BigDecimal.ZERO,
                    "Jackpot configuration not found"
            );
        }

        Jackpot jackpot = jackpotOpt.get();
        boolean won = calculateWinProbability(jackpot);

        if (won) {
            BigDecimal rewardAmount = jackpot.getCurrentPool();

            // Create reward record
            JackpotReward reward = new JackpotReward();
            reward.setBetId(betId);
            reward.setUserId(contribution.getUserId());
            reward.setJackpotId(jackpot.getJackpotId());
            reward.setJackpotRewardAmount(rewardAmount);
            reward.setCreatedAt(LocalDateTime.now());
            rewardRepository.save(reward);

            // Reset jackpot to initial pool value
            jackpot.setCurrentPool(jackpot.getInitialPool());
            jackpotRepository.save(jackpot);

            log.info("Jackpot won! Bet: {}, Amount: {}", betId, rewardAmount);

            return new RewardEvaluationResponse(
                    true,
                    rewardAmount,
                    "Congratulations! You won the jackpot!"
            );
        } else {
            return new RewardEvaluationResponse(
                    false,
                    BigDecimal.ZERO,
                    "Better luck next time!"
            );
        }
    }

    private boolean calculateWinProbability(Jackpot jackpot) {
        RewardStrategy strategy = strategyFactory.getStrategy(jackpot.getRewardType());
        BigDecimal winChance = strategy.calculateWinChance(jackpot);

        // Generate random number between 0 and 100
        double random = Math.random() * 100;
        boolean won = random < winChance.doubleValue();

        log.debug("Win chance: {}%, Random: {}, Won: {}", winChance, random, won);
        return won;
    }
}