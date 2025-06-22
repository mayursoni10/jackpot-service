package com.sporty.jackpot.service;

import com.sporty.jackpot.dto.RewardEvaluationResponse;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.JackpotContribution;
import com.sporty.jackpot.entity.JackpotReward;
import com.sporty.jackpot.entity.RewardType;
import com.sporty.jackpot.factory.RewardStrategyFactory;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import com.sporty.jackpot.strategy.reward.RewardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JackpotRewardServiceTest {

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JackpotContributionRepository contributionRepository;

    @Mock
    private JackpotRewardRepository rewardRepository;

    @Mock
    private RewardStrategyFactory strategyFactory;

    @InjectMocks
    private JackpotRewardService rewardService;

    private JackpotContribution testContribution;
    private Jackpot fixedJackpot;
    private Jackpot variableJackpot;

    @BeforeEach
    void setUp() {
        // Setup test contribution
        testContribution = new JackpotContribution();
        testContribution.setBetId("BET-001");
        testContribution.setUserId("USER-001");
        testContribution.setJackpotId("JACKPOT-001");
        testContribution.setStakeAmount(new BigDecimal("100"));
        testContribution.setContributionAmount(new BigDecimal("5"));
        testContribution.setCurrentJackpotAmount(new BigDecimal("1005"));
        testContribution.setCreatedAt(LocalDateTime.now());

        // Setup fixed reward jackpot
        fixedJackpot = new Jackpot();
        fixedJackpot.setJackpotId("JACKPOT-001");
        fixedJackpot.setCurrentPool(new BigDecimal("5000"));
        fixedJackpot.setInitialPool(new BigDecimal("1000"));
        fixedJackpot.setRewardType(RewardType.FIXED);
        fixedJackpot.setRewardChancePercentage(new BigDecimal("0.1")); // 0.1%

        // Setup variable reward jackpot
        variableJackpot = new Jackpot();
        variableJackpot.setJackpotId("JACKPOT-002");
        variableJackpot.setCurrentPool(new BigDecimal("25000"));
        variableJackpot.setInitialPool(new BigDecimal("5000"));
        variableJackpot.setRewardType(RewardType.VARIABLE);
        variableJackpot.setInitialRewardChancePercentage(new BigDecimal("0.1"));
        variableJackpot.setRewardChanceIncreaseRate(new BigDecimal("0.002"));
        variableJackpot.setPoolLimitFor100PercentChance(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should handle bet not found")
    void evaluateReward_BetNotFound_ReturnsNotEligible() {
        // Given
        when(rewardRepository.findByBetId("INVALID-BET")).thenReturn(Optional.empty());
        when(contributionRepository.findByBetId("INVALID-BET")).thenReturn(Optional.empty());

        // When
        RewardEvaluationResponse response = rewardService.evaluateReward("INVALID-BET");

        // Then
        assertThat(response.isWinner()).isFalse();
        assertThat(response.getRewardAmount()).isEqualByComparingTo("0");
        assertThat(response.getMessage()).contains("not found or not eligible");

        verify(strategyFactory, never()).getStrategy(any());
    }

    @Test
    @DisplayName("Should handle already evaluated bet")
    void evaluateReward_AlreadyEvaluated_ReturnsExistingReward() {
        // Given
        JackpotReward existingReward = new JackpotReward();
        existingReward.setBetId("BET-001");
        existingReward.setJackpotRewardAmount(new BigDecimal("5000"));

        when(rewardRepository.findByBetId("BET-001")).thenReturn(Optional.of(existingReward));

        // When
        RewardEvaluationResponse response = rewardService.evaluateReward("BET-001");

        // Then
        assertThat(response.isWinner()).isTrue();
        assertThat(response.getRewardAmount()).isEqualByComparingTo("5000");
        assertThat(response.getMessage()).contains("already awarded");

        verify(contributionRepository, never()).findByBetId(any());
        verify(strategyFactory, never()).getStrategy(any());
    }

    @Test
    @DisplayName("Should process fixed reward win")
    void evaluateReward_FixedReward_Win() {
        // Given
        when(rewardRepository.findByBetId("BET-001")).thenReturn(Optional.empty());
        when(contributionRepository.findByBetId("BET-001")).thenReturn(Optional.of(testContribution));
        when(jackpotRepository.findById("JACKPOT-001")).thenReturn(Optional.of(fixedJackpot));
        when(rewardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Mock strategy to guarantee win for testing
        RewardStrategy fixedStrategy = mock(RewardStrategy.class);
        when(fixedStrategy.calculateWinChance(fixedJackpot)).thenReturn(new BigDecimal("100")); // 100% to force win
        when(strategyFactory.getStrategy(RewardType.FIXED)).thenReturn(fixedStrategy);

        // When
        RewardEvaluationResponse response = rewardService.evaluateReward("BET-001");

        // Then
        assertThat(response.isWinner()).isTrue();
        assertThat(response.getRewardAmount()).isEqualByComparingTo("5000");
        assertThat(response.getMessage()).contains("Congratulations");

        // Verify reward saved
        ArgumentCaptor<JackpotReward> rewardCaptor = ArgumentCaptor.forClass(JackpotReward.class);
        verify(rewardRepository).save(rewardCaptor.capture());
        JackpotReward savedReward = rewardCaptor.getValue();
        assertThat(savedReward.getJackpotRewardAmount()).isEqualByComparingTo("5000");

        // Verify jackpot reset
        verify(jackpotRepository).save(argThat(jackpot ->
                jackpot.getCurrentPool().compareTo(new BigDecimal("1000")) == 0
        ));
    }

    @Test
    @DisplayName("Should process fixed reward loss")
    void evaluateReward_FixedReward_Loss() {
        // Given
        when(rewardRepository.findByBetId("BET-002")).thenReturn(Optional.empty());
        when(contributionRepository.findByBetId("BET-002")).thenReturn(Optional.of(testContribution));
        when(jackpotRepository.findById("JACKPOT-001")).thenReturn(Optional.of(fixedJackpot));

        // Mock strategy to guarantee loss for testing
        RewardStrategy fixedStrategy = mock(RewardStrategy.class);
        when(fixedStrategy.calculateWinChance(fixedJackpot)).thenReturn(new BigDecimal("0")); // 0% to force loss
        when(strategyFactory.getStrategy(RewardType.FIXED)).thenReturn(fixedStrategy);

        // When
        RewardEvaluationResponse response = rewardService.evaluateReward("BET-002");

        // Then
        assertThat(response.isWinner()).isFalse();
        assertThat(response.getRewardAmount()).isEqualByComparingTo("0");
        assertThat(response.getMessage()).contains("Better luck");

        // Verify no reward saved and jackpot not reset
        verify(rewardRepository, never()).save(any());
        verify(jackpotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate variable reward chance correctly")
    void evaluateReward_VariableReward_CalculatesChanceCorrectly() {
        // Given
        testContribution.setJackpotId("JACKPOT-002");
        when(rewardRepository.findByBetId("BET-003")).thenReturn(Optional.empty());
        when(contributionRepository.findByBetId("BET-003")).thenReturn(Optional.of(testContribution));
        when(jackpotRepository.findById("JACKPOT-002")).thenReturn(Optional.of(variableJackpot));

        // Mock variable strategy with actual calculation
        RewardStrategy variableStrategy = mock(RewardStrategy.class);
        // At 25000 pool (halfway to limit), should return ~5% chance
        when(variableStrategy.calculateWinChance(variableJackpot)).thenReturn(new BigDecimal("5.05"));
        when(strategyFactory.getStrategy(RewardType.VARIABLE)).thenReturn(variableStrategy);

        // When
        rewardService.evaluateReward("BET-003");

        // Then
        verify(strategyFactory).getStrategy(RewardType.VARIABLE);
        verify(variableStrategy).calculateWinChance(variableJackpot);
    }

    @Test
    @DisplayName("Should guarantee win at pool limit")
    void evaluateReward_VariableReward_PoolLimitGuaranteesWin() {
        // Given
        variableJackpot.setCurrentPool(new BigDecimal("50000")); // At limit
        testContribution.setJackpotId("JACKPOT-002");

        when(rewardRepository.findByBetId("BET-004")).thenReturn(Optional.empty());
        when(contributionRepository.findByBetId("BET-004")).thenReturn(Optional.of(testContribution));
        when(jackpotRepository.findById("JACKPOT-002")).thenReturn(Optional.of(variableJackpot));
        when(rewardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Mock variable strategy to return 100% at pool limit
        RewardStrategy variableStrategy = mock(RewardStrategy.class);
        when(variableStrategy.calculateWinChance(variableJackpot)).thenReturn(new BigDecimal("100"));
        when(strategyFactory.getStrategy(RewardType.VARIABLE)).thenReturn(variableStrategy);

        // When
        RewardEvaluationResponse response = rewardService.evaluateReward("BET-004");

        // Then
        assertThat(response.isWinner()).isTrue();
        assertThat(response.getRewardAmount()).isEqualByComparingTo("50000");

        // Verify jackpot reset to initial value
        verify(jackpotRepository).save(argThat(jackpot ->
                jackpot.getCurrentPool().compareTo(new BigDecimal("5000")) == 0
        ));
    }
}

