package com.example.medhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class MedHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedHubApplication.class, args);
    }

}
