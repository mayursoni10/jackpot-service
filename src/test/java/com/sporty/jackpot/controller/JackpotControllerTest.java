package com.sporty.jackpot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.jackpot.dto.BetRequest;
import com.sporty.jackpot.dto.JackpotRewardResponse;
import com.sporty.jackpot.service.BetService;
import com.sporty.jackpot.service.JackpotRewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
public class JackpotControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BetService betService;

    @Mock
    private JackpotRewardService rewardService;

    @InjectMocks
    private JackpotController jackpotController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jackpotController)
                .build();
    }

    @Test
    void publishBet_ValidRequest_ReturnsSuccess() throws Exception {
        BetRequest betRequest = new BetRequest();
        betRequest.setBetId("BET-001");
        betRequest.setUserId("USER-001");
        betRequest.setJackpotId("JACKPOT-001");
        betRequest.setBetAmount(new BigDecimal("100.00"));

        doNothing().when(betService).publishBet(any(BetRequest.class));

        mockMvc.perform(post("/api/v1/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bet published successfully"));

        verify(betService).publishBet(any(BetRequest.class));
    }

    @Test
    void evaluateJackpotReward_WinningBet_ReturnsWinResponse() throws Exception {
        JackpotRewardResponse winResponse = JackpotRewardResponse.builder()
                .won(true)
                .rewardAmount(new BigDecimal("5000.00"))
                .message("Congratulations! You won the jackpot!")
                .build();

        when(rewardService.evaluateJackpotReward("BET-001")).thenReturn(winResponse);

        mockMvc.perform(get("/api/v1/bets/BET-001/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.rewardAmount").value(5000.00))
                .andExpect(jsonPath("$.message").value("Congratulations! You won the jackpot!"));

        verify(rewardService).evaluateJackpotReward("BET-001");
    }

    @Test
    void evaluateJackpotReward_LosingBet_ReturnsLoseResponse() throws Exception {
        JackpotRewardResponse loseResponse = JackpotRewardResponse.builder()
                .won(false)
                .rewardAmount(BigDecimal.ZERO)
                .message("Better luck next time!")
                .build();

        when(rewardService.evaluateJackpotReward("BET-002")).thenReturn(loseResponse);

        mockMvc.perform(get("/api/v1/bets/BET-002/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(false))
                .andExpect(jsonPath("$.rewardAmount").value(0))
                .andExpect(jsonPath("$.message").value("Better luck next time!"));
    }

    @Test
    void evaluateJackpotReward_NonExistentBet_ReturnsNotFound() throws Exception {
        JackpotRewardResponse notFoundResponse = JackpotRewardResponse.builder()
                .won(false)
                .rewardAmount(BigDecimal.ZERO)
                .message("Bet not found or not eligible for jackpot")
                .build();

        when(rewardService.evaluateJackpotReward("NON-EXISTENT")).thenReturn(notFoundResponse);

        mockMvc.perform(get("/api/v1/bets/NON-EXISTENT/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(false))
                .andExpect(jsonPath("$.message").value("Bet not found or not eligible for jackpot"));
    }
}
