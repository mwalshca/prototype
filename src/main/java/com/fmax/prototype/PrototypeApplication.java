package com.fmax.prototype;

import org.springframework.boot.SpringApplication;

//@SpringBootApplication
public class PrototypeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrototypeApplication.class, args);
	}

	/*@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}

		};
	}*/
}
