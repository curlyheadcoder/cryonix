package com.trading_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CryonixApplication {

	public static void main(String[] args) {
		System.out.println("Cryonix Backend");
		SpringApplication.run(CryonixApplication.class, args);
	}

}
