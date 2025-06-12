package com.sporty.jackpot.service;

import com.sporty.jackpot.dto.JackpotRewardResponse;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.JackpotReward;
import com.sporty.jackpot.entity.RewardType;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class JackpotRewardService {
    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository contributionRepository;
    private final JackpotRewardRepository rewardRepository;
    private final Random random = new Random();

    @Transactional
    public JackpotRewardResponse evaluateJackpotReward(String betId) {
        return contributionRepository.findByBetId(betId)
                .map(contribution -> {
                    Jackpot jackpot = jackpotRepository.findById(contribution.getJackpotId())
                            .orElseThrow(() -> new RuntimeException("Jackpot not found"));

                    BigDecimal winChance = calculateWinChance(jackpot);
                    boolean won = checkIfWon(winChance);

                    if (won) {
                        BigDecimal rewardAmount = jackpot.getCurrentPool();

                        // Create reward record
                        JackpotReward reward = new JackpotReward();
                        reward.setBetId(betId);
                        reward.setUserId(contribution.getUserId());
                        reward.setJackpotId(jackpot.getJackpotId());
                        reward.setJackpotRewardAmount(rewardAmount);
                        rewardRepository.save(reward);

                        // Reset jackpot to initial pool
                        jackpot.setCurrentPool(jackpot.getInitialPool());
                        jackpotRepository.save(jackpot);

                        return JackpotRewardResponse.builder()
                                .won(true)
                                .rewardAmount(rewardAmount)
                                .message("Congratulations! You won the jackpot!")
                                .build();
                    }

                    return JackpotRewardResponse.builder()
                            .won(false)
                            .rewardAmount(BigDecimal.ZERO)
                            .message("Better luck next time!")
                            .build();
                })
                .orElse(JackpotRewardResponse.builder()
                        .won(false)
                        .rewardAmount(BigDecimal.ZERO)
                        .message("Bet not found or not eligible for jackpot")
                        .build());
    }

    private BigDecimal calculateWinChance(Jackpot jackpot) {
        if (jackpot.getRewardType() == RewardType.FIXED) {
            return jackpot.getRewardChancePercentage();
        }

        // Variable reward - increases as pool increases
        BigDecimal poolIncrease = jackpot.getCurrentPool().subtract(jackpot.getInitialPool());

        if (jackpot.getPoolLimitFor100PercentChance() != null &&
                jackpot.getCurrentPool().compareTo(jackpot.getPoolLimitFor100PercentChance()) >= 0) {
            return new BigDecimal("100");
        }

        BigDecimal increaseAmount = poolIncrease.multiply(jackpot.getRewardChanceIncreaseRate())
                .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);

        BigDecimal chance = jackpot.getInitialRewardChancePercentage().add(increaseAmount);
        return chance.min(new BigDecimal("100"));
    }

    private boolean checkIfWon(BigDecimal winChancePercentage) {
        double randomValue = random.nextDouble() * 100;
        return randomValue < winChancePercentage.doubleValue();
    }
}
