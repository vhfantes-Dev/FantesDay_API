package com.vhfantes.fantesday;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FantesdayApplication {

	// Fuso usado por LocalDate.now(), LocalDateTime.now() e @CreationTimestamp. Servidores como o Render
	// rodam em UTC, o que adiantava o "hoje" e as horas em 3h para quem esta no Brasil.
	static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";

	public static void main(String[] args) {
		String zone = System.getenv("APP_TIMEZONE");
		TimeZone.setDefault(TimeZone.getTimeZone(zone != null && !zone.isBlank() ? zone : DEFAULT_TIMEZONE));
		SpringApplication.run(FantesdayApplication.class, args);
	}

}
