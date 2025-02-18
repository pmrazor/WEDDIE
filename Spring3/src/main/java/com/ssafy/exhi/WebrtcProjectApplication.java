package com.ssafy.exhi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication
public class WebrtcProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebrtcProjectApplication.class, args);
	}

}
