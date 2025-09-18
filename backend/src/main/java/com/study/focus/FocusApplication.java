package com.study.focus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FocusApplication {

	public static void main(String[] args) {
		SpringApplication.run(FocusApplication.class, args);
	}

}
