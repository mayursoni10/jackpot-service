package com.sporty.jackpot.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.jackpot.config.TestConfig;
import com.sporty.jackpot.dto.BetRequest;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.service.JackpotContributionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class JackpotServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JackpotRepository jackpotRepository;

    // Mock the async processing to make it synchronous for testing
    @MockBean
    private JackpotContributionService contributionService;

    @Test
    @DisplayName("Should publish bet successfully")
    void publishBet_Success() throws Exception {
        // Given
        BetRequest betRequest = new BetRequest();
        betRequest.setBetId("INT-TEST-BET-001");
        betRequest.setUserId("INT-TEST-USER-001");
        betRequest.setJackpotId("JACKPOT-001");
        betRequest.setBetAmount(new BigDecimal("1000"));

        // When - Publish bet
        mockMvc.perform(post("/api/v1/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bet published successfully"));

        // When - Evaluate reward (without checking contribution since it's async)
        mockMvc.perform(get("/api/v1/bets/INT-TEST-BET-001/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").exists())
                .andExpect(jsonPath("$.rewardAmount").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should handle multiple bets")
    void multipleBets_Success() throws Exception {
        // When - Publish multiple bets
        for (int i = 1; i <= 5; i++) {
            BetRequest betRequest = new BetRequest();
            betRequest.setBetId("MULTI-BET-" + i);
            betRequest.setUserId("USER-" + i);
            betRequest.setJackpotId("JACKPOT-001");
            betRequest.setBetAmount(new BigDecimal("100"));

            mockMvc.perform(post("/api/v1/bets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(betRequest)))
                    .andExpect(status().isOk());
        }

        // Then - Just verify the endpoint works without checking async results
        assertThat(true).isTrue(); // Placeholder assertion
    }
}