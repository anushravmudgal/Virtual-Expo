package com.ims.expo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VirtualExpoApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualExpoApplication.class, args);
	}

}
