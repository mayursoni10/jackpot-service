package com.sporty.jackpot;

import com.sporty.jackpot.service.MockKafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
@Import(JackpotApplicationTests.MockConfig.class)
class JackpotApplicationTests {

	@TestConfiguration
	@Profile("test")
	static class MockConfig {

		@Bean
		@Primary
		public KafkaTemplate<String, Object> mockKafkaTemplate() {
			return mock(KafkaTemplate.class);
		}

		@Bean
		@Primary
		public ProducerFactory<String, Object> mockProducerFactory() {
			return mock(ProducerFactory.class);
		}

		@Bean
		@Primary
		public ConsumerFactory<String, Object> mockConsumerFactory() {
			return mock(ConsumerFactory.class);
		}

		@Bean
		@Primary
		public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
			ConcurrentKafkaListenerContainerFactory<String, Object> factory =
					mock(ConcurrentKafkaListenerContainerFactory.class);
			return factory;
		}
	}

	@Test
	void contextLoads() {
		// Application context should load successfully
	}
}
