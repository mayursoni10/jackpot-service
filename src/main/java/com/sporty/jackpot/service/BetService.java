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

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private static final String TOPIC = "jackpot-bets";

    public void publishBet(BetRequest betRequest) {
        Bet bet = new Bet(
                betRequest.getBetId(),
                betRequest.getUserId(),
                betRequest.getJackpotId(),
                betRequest.getBetAmount()
        );

        // Mock Kafka producer - just log the payload and publish event
        log.info("Publishing bet to Kafka topic '{}': {}", TOPIC, bet);
        eventPublisher.publishEvent(new BetEvent(bet));

        // Uncomment below line for actual Kafka integration
        // kafkaTemplate.send(TOPIC, bet.getBetId(), bet);
    }
}
