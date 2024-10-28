package com.readmate.ReadMate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ReadMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadMateApplication.class, args);
	}

}
