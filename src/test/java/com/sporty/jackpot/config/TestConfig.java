package com.sporty.jackpot.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("test")
public class TestConfig {
    @Bean
    @Primary
    public KafkaTemplate<String, Object> mockKafkaTemplate() {
        KafkaTemplate<String, Object> mockTemplate = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(mockTemplate.send(anyString(), any())).thenReturn(future);
        when(mockTemplate.send(anyString(), anyString(), any())).thenReturn(future);
        return mockTemplate;
    }
}
