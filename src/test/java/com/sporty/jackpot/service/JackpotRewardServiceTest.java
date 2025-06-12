package com.sporty.jackpot.service;

import com.sporty.jackpot.dto.JackpotRewardResponse;
import com.sporty.jackpot.entity.*;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JackpotRewardServiceTest {
    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JackpotContributionRepository contributionRepository;

    @Mock
    private JackpotRewardRepository rewardRepository;

    @Mock
    private Random random;

    @InjectMocks
    private JackpotRewardService rewardService;

    private JackpotContribution testContribution;
    private Jackpot fixedRewardJackpot;
    private Jackpot variableRewardJackpot;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rewardService, "random", random);

        testContribution = new JackpotContribution();
        testContribution.setBetId("BET-001");
        testContribution.setUserId("USER-001");
        testContribution.setJackpotId("JACKPOT-001");
        testContribution.setStakeAmount(new BigDecimal("100"));
        testContribution.setContributionAmount(new BigDecimal("5"));

        fixedRewardJackpot = new Jackpot();
        fixedRewardJackpot.setJackpotId("JACKPOT-001");
        fixedRewardJackpot.setCurrentPool(new BigDecimal("5000"));
        fixedRewardJackpot.setInitialPool(new BigDecimal("1000"));
        fixedRewardJackpot.setRewardType(RewardType.FIXED);
        fixedRewardJackpot.setRewardChancePercentage(new BigDecimal("1"));

        variableRewardJackpot = new Jackpot();
        variableRewardJackpot.setJackpotId("JACKPOT-002");
        variableRewardJackpot.setCurrentPool(new BigDecimal("20000"));
        variableRewardJackpot.setInitialPool(new BigDecimal("5000"));
        variableRewardJackpot.setRewardType(RewardType.VARIABLE);
        variableRewardJackpot.setInitialRewardChancePercentage(new BigDecimal("0.1"));
        variableRewardJackpot.setRewardChanceIncreaseRate(new BigDecimal("0.01"));
        variableRewardJackpot.setPoolLimitFor100PercentChance(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should evaluate winning bet and reset jackpot")
    void evaluateJackpotReward_WinningBet_ReturnsRewardAndResetsPool() {
        // Given
        when(contributionRepository.findByBetId("BET-001"))
                .thenReturn(Optional.of(testContribution));
        when(jackpotRepository.findById("JACKPOT-001"))
                .thenReturn(Optional.of(fixedRewardJackpot));
        when(random.nextDouble()).thenReturn(0.005); // 0.5% < 1% = WIN

        // When
        JackpotRewardResponse response = rewardService.evaluateJackpotReward("BET-001");

        // Then
        assertThat(response.isWon()).isTrue();
        assertThat(response.getRewardAmount()).isEqualByComparingTo("5000");
        assertThat(response.getMessage()).contains("Congratulations");

        verify(rewardRepository).save(argThat(reward ->
                reward.getJackpotRewardAmount().compareTo(new BigDecimal("5000")) == 0
        ));

        verify(jackpotRepository).save(argThat(jackpot ->
                jackpot.getCurrentPool().compareTo(new BigDecimal("1000")) == 0
        ));
    }

    @Test
    @DisplayName("Should evaluate losing bet")
    void evaluateJackpotReward_LosingBet_NoReward() {
        // Given
        when(contributionRepository.findByBetId("BET-001"))
                .thenReturn(Optional.of(testContribution));
        when(jackpotRepository.findById("JACKPOT-001"))
                .thenReturn(Optional.of(fixedRewardJackpot));
        when(random.nextDouble()).thenReturn(0.02); // 2% > 1% = LOSE

        // When
        JackpotRewardResponse response = rewardService.evaluateJackpotReward("BET-001");

        // Then
        assertThat(response.isWon()).isFalse();
        assertThat(response.getRewardAmount()).isEqualByComparingTo("0");
        assertThat(response.getMessage()).contains("Better luck");

        verify(rewardRepository, never()).save(any());
        verify(jackpotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle bet not found")
    void evaluateJackpotReward_BetNotFound_ReturnsNotEligible() {
        // Given
        when(contributionRepository.findByBetId("INVALID-BET"))
                .thenReturn(Optional.empty());

        // When
        JackpotRewardResponse response = rewardService.evaluateJackpotReward("INVALID-BET");

        // Then
        assertThat(response.isWon()).isFalse();
        assertThat(response.getMessage()).contains("not found");
    }

    @Test
    @DisplayName("Should calculate variable reward chance correctly")
    void evaluateJackpotReward_VariableReward_IncreasesWithPool() {
        // Given
        testContribution.setJackpotId("JACKPOT-002");
        when(contributionRepository.findByBetId("BET-002"))
                .thenReturn(Optional.of(testContribution));
        when(jackpotRepository.findById("JACKPOT-002"))
                .thenReturn(Optional.of(variableRewardJackpot));
        when(random.nextDouble()).thenReturn(0.002); // Will win based on calculation

        // When
        JackpotRewardResponse response = rewardService.evaluateJackpotReward("BET-002");

        // Then
        assertThat(response.isWon()).isTrue();
        assertThat(response.getRewardAmount()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("Should guarantee win at pool limit")
    void evaluateJackpotReward_PoolLimit_Guaranteed100PercentChance() {
        // Given
        variableRewardJackpot.setCurrentPool(new BigDecimal("50000"));
        testContribution.setJackpotId("JACKPOT-002");

        when(contributionRepository.findByBetId("BET-003"))
                .thenReturn(Optional.of(testContribution));
        when(jackpotRepository.findById("JACKPOT-002"))
                .thenReturn(Optional.of(variableRewardJackpot));
        when(random.nextDouble()).thenReturn(0.99); // Any value < 100%

        // When
        JackpotRewardResponse response = rewardService.evaluateJackpotReward("BET-003");

        // Then
        assertThat(response.isWon()).isTrue();
    }
}
