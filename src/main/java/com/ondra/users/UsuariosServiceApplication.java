package com.ondra.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase principal de la aplicación Usuarios Service.
 *
 * <p>Se encarga de inicializar la aplicación Spring Boot y habilitar la programación
 * de tareas programadas mediante {@link EnableScheduling}.</p>
 *
 * <p>También añade logs de arranque para indicar cuándo se inicia y termina la inicialización.</p>
 */
@SpringBootApplication
@EnableScheduling
@Slf4j
public class UsuariosServiceApplication {

	/**
	 * Método principal que arranca la aplicación.
	 *
	 * @param args Argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(UsuariosServiceApplication.class, args);
	}
}