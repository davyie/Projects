package com.roadmap.schedule.service;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
	ScheduleRepository scheduleRepository;

	@MockitoBean
	TimeSlotEntityRepository timeSlotEntityRepository;

	@MockitoBean
	RabbitTemplate rabbitTemplate;

	@Test
	void contextLoads() {
	}

}
