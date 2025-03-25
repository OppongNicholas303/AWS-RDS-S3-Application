package com.example.All.in.one;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class AllInOneApplicationTests {

	@Test
	void contextLoads() {
		// Intentionally empty test to verify context loading
	}
}