package com.greenstock.ms.ofertas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsOfertasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsOfertasApplication.class, args);
	}

}
