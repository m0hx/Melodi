package com.ga.melodi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MelodiApplication {

	public static void main(String[] args) {
		System.out.println("MelodiApplication started!");
		System.out.println("test!");
		SpringApplication.run(MelodiApplication.class, args);
	}

}
