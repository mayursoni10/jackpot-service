package com.sporty.jackpot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MockKafkaConsumer {
    private final JackpotContributionService contributionService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleBetEvent(BetEvent event) {
        log.info("Mock Kafka Consumer received bet: {}", event.getBet());
        contributionService.processBet(event.getBet());
    }
}
