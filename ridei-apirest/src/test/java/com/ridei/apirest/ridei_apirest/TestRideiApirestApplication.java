package com.ridei.apirest.ridei_apirest;

import org.springframework.boot.SpringApplication;

public class TestRideiApirestApplication {

	public static void main(String[] args) {
		SpringApplication.from(RideiApirestApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
