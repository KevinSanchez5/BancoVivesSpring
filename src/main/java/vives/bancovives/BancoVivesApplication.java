package vives.bancovives;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BancoVivesApplication {

	public static void main(String[] args) {
		SpringApplication.run(BancoVivesApplication.class, args);
	}

}
