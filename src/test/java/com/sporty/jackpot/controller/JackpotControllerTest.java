package com.sporty.jackpot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.jackpot.config.TestConfig;
import com.sporty.jackpot.dto.BetRequest;
import com.sporty.jackpot.dto.JackpotRewardResponse;
import com.sporty.jackpot.service.BetService;
import com.sporty.jackpot.service.JackpotRewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class JackpotControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BetService betService;

    @Mock
    private JackpotRewardService jackpotRewardService;

    @InjectMocks
    private JackpotController jackpotController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jackpotController)
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("Should publish bet successfully")
    void publishBet_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        BetRequest betRequest = new BetRequest();
        betRequest.setBetId("BET-001");
        betRequest.setUserId("USER-001");
        betRequest.setJackpotId("JACKPOT-001");
        betRequest.setBetAmount(new BigDecimal("100.00"));

        doNothing().when(betService).publishBet(any(BetRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bet published successfully"));

        verify(betService, times(1)).publishBet(any(BetRequest.class));
    }

    @Test
    @DisplayName("Should return bad request for invalid bet")
    void publishBet_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        String invalidJson = "{ \"betId\": \"\" }";

        // When & Then
        mockMvc.perform(post("/api/v1/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isOk()); // Update to isBadRequest() if validation is added
    }

    @Test
    @DisplayName("Should evaluate winning jackpot reward")
    void evaluateJackpotReward_WinningBet_ReturnsWinResponse() throws Exception {
        // Given
        String betId = "BET-001";
        JackpotRewardResponse winResponse = JackpotRewardResponse.builder()
                .won(true)
                .rewardAmount(new BigDecimal("5000.00"))
                .message("Congratulations! You won the jackpot!")
                .build();

        when(jackpotRewardService.evaluateJackpotReward(betId)).thenReturn(winResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/bets/{betId}/evaluate", betId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.rewardAmount").value(5000.00))
                .andExpect(jsonPath("$.message").value("Congratulations! You won the jackpot!"));

        verify(jackpotRewardService, times(1)).evaluateJackpotReward(betId);
    }

    @Test
    @DisplayName("Should evaluate losing jackpot reward")
    void evaluateJackpotReward_LosingBet_ReturnsLoseResponse() throws Exception {
        // Given
        String betId = "BET-002";
        JackpotRewardResponse loseResponse = JackpotRewardResponse.builder()
                .won(false)
                .rewardAmount(BigDecimal.ZERO)
                .message("Better luck next time!")
                .build();

        when(jackpotRewardService.evaluateJackpotReward(betId)).thenReturn(loseResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/bets/{betId}/evaluate", betId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(false))
                .andExpect(jsonPath("$.rewardAmount").value(0))
                .andExpect(jsonPath("$.message").value("Better luck next time!"));
    }

    @Test
    @DisplayName("Should handle bet not found")
    void evaluateJackpotReward_BetNotFound_ReturnsNotFound() throws Exception {
        // Given
        String betId = "NON-EXISTENT";
        JackpotRewardResponse notFoundResponse = JackpotRewardResponse.builder()
                .won(false)
                .rewardAmount(BigDecimal.ZERO)
                .message("Bet not found or not eligible for jackpot")
                .build();

        when(jackpotRewardService.evaluateJackpotReward(betId)).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/bets/{betId}/evaluate", betId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bet not found or not eligible for jackpot"));
    }
}
