package com.cobranca.concentrapay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConcentrapayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcentrapayApplication.class, args);
	}

}
