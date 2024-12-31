package com.kolade.backt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
public class BacktApplication {

	public static void main(String[] args) {
		SpringApplication.run(BacktApplication.class, args);
	}

}
