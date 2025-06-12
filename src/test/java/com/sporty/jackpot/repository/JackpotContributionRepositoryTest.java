package com.sporty.jackpot.repository;

import com.sporty.jackpot.entity.JackpotContribution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@ActiveProfiles("test")
public class JackpotContributionRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JackpotContributionRepository repository;

    @Test
    void findByBetId_ExistingBet_ReturnsContribution() {
        // Given
        JackpotContribution contribution = new JackpotContribution();
        contribution.setBetId("TEST-BET-123");
        contribution.setUserId("USER-123");
        contribution.setJackpotId("JACKPOT-123");
        contribution.setStakeAmount(new BigDecimal("100"));
        contribution.setContributionAmount(new BigDecimal("5"));
        contribution.setCurrentJackpotAmount(new BigDecimal("1005"));

        entityManager.persistAndFlush(contribution);

        // When
        Optional<JackpotContribution> found = repository.findByBetId("TEST-BET-123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("USER-123");
        assertThat(found.get().getContributionAmount()).isEqualByComparingTo("5");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void findByBetId_NonExistingBet_ReturnsEmpty() {
        // When
        Optional<JackpotContribution> found = repository.findByBetId("NON-EXISTING");

        // Then
        assertThat(found).isEmpty();
    }
}
