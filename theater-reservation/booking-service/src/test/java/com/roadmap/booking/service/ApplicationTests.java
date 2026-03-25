package com.roadmap.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
		"org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration," +
		"org.springframework.boot.data.r2dbc.autoconfigure.DataR2dbcAutoConfiguration," +
		"org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration"
})
@TestPropertySource(properties = "spring.sql.init.mode=never")
class ApplicationTests {

	@MockitoBean
	BookingRepository bookingRepository;

	@Test
	void contextLoads() {
	}

}
