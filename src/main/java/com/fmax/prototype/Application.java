package com.fmax.prototype;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.fmax.prototype.services.TradeGovernor;

@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			ctx.getBean(TradeGovernor.class);
		};
	}

	//    startStream(ibs, "TLRY", "SMART", "TLRY");
	//  startStream(ibs, "BLDP", "SMART", "BLDP");
	// startStream(ibs, "ABX", "SMART", "GOLD");
	// startStream(ibs, "SHOP", "SMART", "SHOP");
	// startStream(ibs, "BTO", "SMART", "BTG");		
	
	// RY ISN = CA7800871021
}
