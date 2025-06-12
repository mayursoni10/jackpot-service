package com.sporty.jackpot.repository;

import com.sporty.jackpot.entity.Jackpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotRepository extends JpaRepository<Jackpot, String> {
}
