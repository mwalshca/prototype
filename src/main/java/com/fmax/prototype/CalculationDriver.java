package com.fmax.prototype;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.fmax.prototype.services.CalculationService;

@SpringBootApplication
@Service
public class CalculationDriver {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	/*
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			CalculationDriver driver = ctx.getBean(CalculationDriver.class);
		};
	}
	
	*/	
	CalculationDriver(CalculationService calculationService){
		this.calculationService = calculationService;
	}
	CalculationService calculationService;

	
	
}
