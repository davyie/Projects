package com.roadmap.theater_reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class TheaterReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheaterReservationApplication.class, args);
	}

}
