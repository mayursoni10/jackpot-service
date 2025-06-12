package com.sporty.jackpot.service;

import com.sporty.jackpot.entity.ContributionType;
import com.sporty.jackpot.entity.Jackpot;
import com.sporty.jackpot.entity.JackpotContribution;
import com.sporty.jackpot.model.Bet;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class JackpotContributionServiceTest {
    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JackpotContributionRepository contributionRepository;

    @InjectMocks
    private JackpotContributionService contributionService;

    private Bet testBet;
    private Jackpot fixedJackpot;
    private Jackpot variableJackpot;

    @BeforeEach
    void setUp() {
        testBet = new Bet("BET-001", "USER-001", "JACKPOT-001", new BigDecimal("100"));

        // Setup fixed jackpot
        fixedJackpot = new Jackpot();
        fixedJackpot.setJackpotId("JACKPOT-001");
        fixedJackpot.setCurrentPool(new BigDecimal("1000"));
        fixedJackpot.setInitialPool(new BigDecimal("1000"));
        fixedJackpot.setContributionType(ContributionType.FIXED);
        fixedJackpot.setContributionPercentage(new BigDecimal("5"));

        // Setup variable jackpot
        variableJackpot = new Jackpot();
        variableJackpot.setJackpotId("JACKPOT-002");
        variableJackpot.setCurrentPool(new BigDecimal("10000"));
        variableJackpot.setInitialPool(new BigDecimal("5000"));
        variableJackpot.setContributionType(ContributionType.VARIABLE);
        variableJackpot.setInitialContributionPercentage(new BigDecimal("10"));
        variableJackpot.setContributionDecreaseRate(new BigDecimal("0.001"));
    }

    @Test
    @DisplayName("Should process bet with fixed contribution")
    void processBet_FixedContribution_CalculatesCorrectly() {
        // Given
        when(jackpotRepository.findById("JACKPOT-001")).thenReturn(Optional.of(fixedJackpot));
        when(contributionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        contributionService.processBet(testBet);

        // Then
        ArgumentCaptor<JackpotContribution> contributionCaptor =
                ArgumentCaptor.forClass(JackpotContribution.class);
        verify(contributionRepository).save(contributionCaptor.capture());

        JackpotContribution savedContribution = contributionCaptor.getValue();
        assertThat(savedContribution.getBetId()).isEqualTo("BET-001");
        assertThat(savedContribution.getContributionAmount())
                .isEqualByComparingTo("5.00"); // 5% of 100
        assertThat(savedContribution.getCurrentJackpotAmount())
                .isEqualByComparingTo("1005.00"); // 1000 + 5

        verify(jackpotRepository).save(argThat(jackpot ->
                jackpot.getCurrentPool().compareTo(new BigDecimal("1005.00")) == 0
        ));
    }

    @Test
    @DisplayName("Should process bet with variable contribution")
    void processBet_VariableContribution_DecreasesWithPoolSize() {
        // Given
        when(jackpotRepository.findById("JACKPOT-002")).thenReturn(Optional.of(variableJackpot));
        when(contributionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        contributionService.processBet(new Bet("BET-002", "USER-001", "JACKPOT-002",
                new BigDecimal("200")));

        // Then
        ArgumentCaptor<JackpotContribution> contributionCaptor =
                ArgumentCaptor.forClass(JackpotContribution.class);
        verify(contributionRepository).save(contributionCaptor.capture());

        JackpotContribution savedContribution = contributionCaptor.getValue();
        // Expected: Initial 10% - (5000 * 0.001 / 100) = 10% - 0.05% = 9.95%
        // Contribution: 200 * 9.95% = 19.90
        assertThat(savedContribution.getContributionAmount())
                .isEqualByComparingTo("19.90");
    }

    @Test
    @DisplayName("Should handle jackpot not found")
    void processBet_JackpotNotFound_DoesNothing() {
        // Given
        when(jackpotRepository.findById("INVALID-JACKPOT")).thenReturn(Optional.empty());

        // When
        contributionService.processBet(new Bet("BET-003", "USER-001", "INVALID-JACKPOT",
                new BigDecimal("100")));

        // Then
        verify(contributionRepository, never()).save(any());
        verify(jackpotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should enforce minimum contribution percentage")
    void processBet_MinimumContribution_EnforcesOnePercent() {
        // Given - Variable jackpot with very high pool
        variableJackpot.setCurrentPool(new BigDecimal("1000000"));
        when(jackpotRepository.findById("JACKPOT-002")).thenReturn(Optional.of(variableJackpot));
        when(contributionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        contributionService.processBet(new Bet("BET-004", "USER-001", "JACKPOT-002",
                new BigDecimal("100")));

        // Then
        ArgumentCaptor<JackpotContribution> contributionCaptor =
                ArgumentCaptor.forClass(JackpotContribution.class);
        verify(contributionRepository).save(contributionCaptor.capture());

        // Should enforce minimum 1%
        assertThat(contributionCaptor.getValue().getContributionAmount())
                .isEqualByComparingTo("1.00");
    }
}
