package com.sporty.jackpot;

import com.sporty.jackpot.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class JackpotApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads successfully
	}
}
