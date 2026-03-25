package com.roadmap.theater.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
		"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
		"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
		"org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration"
})
class TheaterReservationApplicationTests {

	@Test
	void contextLoads() {
	}

}
