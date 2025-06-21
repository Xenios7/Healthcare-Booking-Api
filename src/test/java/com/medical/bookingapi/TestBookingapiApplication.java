package com.medical.bookingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestBookingapiApplication {

	public static void main(String[] args) {
		SpringApplication.from(BookingapiApplication::main).run(args);
	}

}
