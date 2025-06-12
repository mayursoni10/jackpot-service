package com.sporty.jackpot.repository;

import com.sporty.jackpot.entity.JackpotContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JackpotContributionRepository extends JpaRepository<JackpotContribution, String> {
    Optional<JackpotContribution> findByBetId(String betId);
}
