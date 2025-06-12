package com.sporty.jackpot.integration;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Disabled("Requires Docker environment")
// Or use this to enable only when Docker is available
// @EnabledIfEnvironmentVariable(named = "TESTCONTAINERS_ENABLED", matches = "true")
class JackpotServiceE2ETest {

    @Test
    void testWithRealKafka() {
        // Test implementation
    }
}
