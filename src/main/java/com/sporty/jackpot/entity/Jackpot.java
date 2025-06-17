package com.sporty.jackpot.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "jackpots")
@Data
public class Jackpot {
    @Id
    private String jackpotId;
    private BigDecimal currentPool;
    private BigDecimal initialPool;

    @Enumerated(EnumType.STRING)
    private ContributionType contributionType;

    @Enumerated(EnumType.STRING)
    private RewardType rewardType;

    // Configuration parameters
    private BigDecimal contributionPercentage;
    private BigDecimal initialContributionPercentage;
    private BigDecimal contributionDecreaseRate;

    private BigDecimal rewardChancePercentage;
    private BigDecimal initialRewardChancePercentage;
    private BigDecimal rewardChanceIncreaseRate;
    private BigDecimal poolLimitFor100PercentChance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
