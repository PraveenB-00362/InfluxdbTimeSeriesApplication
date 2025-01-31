package com.timeSeriesApplication.timeSeriesApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeSeriesApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeSeriesApplication.class, args);
	}

}
