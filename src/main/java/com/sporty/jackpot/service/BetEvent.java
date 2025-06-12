package com.sporty.jackpot.service;

import com.sporty.jackpot.model.Bet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BetEvent {
    private Bet bet;
}
