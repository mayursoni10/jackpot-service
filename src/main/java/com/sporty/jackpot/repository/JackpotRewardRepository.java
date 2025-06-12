package com.sporty.jackpot.repository;

import com.sporty.jackpot.entity.JackpotReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotRewardRepository extends JpaRepository<JackpotReward, String> {
}
