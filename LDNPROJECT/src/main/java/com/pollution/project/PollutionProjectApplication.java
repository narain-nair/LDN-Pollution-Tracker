package com.pollution.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PollutionProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(PollutionProjectApplication.class, args);
	}

}
