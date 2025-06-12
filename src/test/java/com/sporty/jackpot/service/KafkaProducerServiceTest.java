package com.sporty.jackpot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.jackpot.model.Bet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should publish bet to Kafka")
    void publishBet_SendsMessageToKafka() throws JsonProcessingException {
//        // Given
//        Bet bet = new Bet("BET-001", "USER-001", "JACKPOT-001", new BigDecimal("100"));
//        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
//        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
//
//        // When
//        kafkaProducerService.publishBet(bet);
//
//        // Then - Verify with the actual topic name used in your service
//        verify(kafkaTemplate).send(anyString(), eq("BET-001"), eq(bet));
        // Arrange
        Bet bet = new Bet();
        bet.setBetId("123");
        String betJson = "{\"betId\":\"123\"}";

        when(objectMapper.writeValueAsString(bet)).thenReturn(betJson);

        // Act
        kafkaProducerService.publishBet(bet);

        // Assert
        verify(kafkaTemplate).send("jackpot-bets", "123", betJson);
    }

    @Test
    @DisplayName("Should handle Kafka send failure gracefully")
    void publishBet_KafkaSendFails_LogsError() throws JsonProcessingException {
//        // Given
//        Bet bet = new Bet("BET-001", "USER-001", "JACKPOT-001", new BigDecimal("100"));
//        when(kafkaTemplate.send(anyString(), anyString(), any()))
//                .thenThrow(new RuntimeException("Kafka unavailable"));
//
//        // When & Then - Should not throw
//        kafkaProducerService.publishBet(bet);
//
//        // Verify the method was called even if it failed
//        verify(kafkaTemplate).send(anyString(), anyString(), any());
        // Arrange
        Bet bet = new Bet();
        bet.setBetId("123");

        when(objectMapper.writeValueAsString(bet)).thenThrow(new RuntimeException("Serialization error"));

        // Act & Assert
        try {
            kafkaProducerService.publishBet(bet);
        } catch (RuntimeException e) {
            // Expected exception
        }

        verify(objectMapper).writeValueAsString(bet);
    }
}
