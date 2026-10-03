package com.temi.banking_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BankingBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BankingBackendApplication.class, args);
	}
}

// psql -U temi_banking -d banking_db