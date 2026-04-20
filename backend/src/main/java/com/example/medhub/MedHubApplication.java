package com.example.medhub;

import com.example.medhub.configuration.MedHubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(MedHubProperties.class)
public class MedHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedHubApplication.class, args);
	}

}

