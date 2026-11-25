package com.lankatrails.lankatrails_backend;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the full application context. Needs the docker-compose.dev.yml services,
 * so it only runs with {@code -Pintegration}.
 */
@Tag("integration")
@SpringBootTest
class LankatrailsBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
