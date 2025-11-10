package com.cachecraft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CacheCraftApplication {

	public static void main(String[] args) {
		SpringApplication.run(CacheCraftApplication.class, args);
	}

}