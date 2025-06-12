package com.sporty.jackpot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.jackpot.model.Bet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "jackpot-bets";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishBet(Bet bet) {
        try {
            String betJson = objectMapper.writeValueAsString(bet);
            logger.info("Publishing bet to Kafka topic '{}': {}", TOPIC, betJson);
            kafkaTemplate.send(TOPIC, bet.getBetId(), betJson);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing bet to JSON", e);
            throw new RuntimeException("Failed to publish bet to Kafka", e);
        }
    }
}
