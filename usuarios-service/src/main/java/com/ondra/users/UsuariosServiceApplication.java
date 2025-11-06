package com.ondra.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Clase principal de la aplicación Usuarios Service.
 */
@SpringBootApplication
@EnableScheduling
@Slf4j
public class UsuariosServiceApplication {

	public static void main(String[] args) {
		log.info("🚀 Iniciando Ondra Usuarios Service...");
		SpringApplication.run(UsuariosServiceApplication.class, args);
		log.info("✅ Ondra Usuarios Service iniciado correctamente");
	}
}