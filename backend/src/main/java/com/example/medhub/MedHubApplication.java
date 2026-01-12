package com.example.medhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MedHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedHubApplication.class, args);
	}

}

