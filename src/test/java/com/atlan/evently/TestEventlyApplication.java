package com.atlan.evently;

import org.springframework.boot.SpringApplication;

public class TestEventlyApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventlyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
