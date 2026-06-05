package com.dhentech.eCommerceCep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ECommerceCepApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceCepApplication.class, args);
	}

}
