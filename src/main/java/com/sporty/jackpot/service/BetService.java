package com.sporty.jackpot.service;


import com.sporty.jackpot.dto.BetRequest;
import com.sporty.jackpot.model.Bet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BetService {

    private final ApplicationEventPublisher eventPublisher;

    public void publishBet(BetRequest betRequest) {
        if (betRequest == null) {
            log.warn("Received null bet request");
            return;
        }

        Bet bet = new Bet(
                betRequest.getBetId(),
                betRequest.getUserId(),
                betRequest.getJackpotId(),
                betRequest.getBetAmount()
        );

        log.info("Publishing bet: {}", bet);
        eventPublisher.publishEvent(new BetEvent(bet));
    }
}
