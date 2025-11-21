package com.ondra.users.data;

import java.util.Arrays;
import java.util.List;

/**
 * Clase que contiene los datos predefinidos de usuarios para el seeding de la base de datos.
 * Incluye información básica de usuarios con nombre, apellidos, username y contraseña.
 */
public class UsuariosData {

    /**
     * Lista de usuarios predefinidos con su información básica.
     * Cada usuario incluye nombre, apellidos, username y contraseña.
     */
    public static final List<UsuarioInfo> USUARIOS_PREDEFINIDOS = Arrays.asList(
            new UsuarioInfo("Ana", "García López", "ana.garcia", "ana"),
            new UsuarioInfo("Carlos", "Martínez Ruiz", "carlos.martinez", "carlos"),
            new UsuarioInfo("Laura", "Rodríguez Sánchez", "laura.rodriguez", "laura"),
            new UsuarioInfo("Miguel", "Fernández Torres", "miguel.fernandez", "miguel"),
            new UsuarioInfo("Sara", "González Pérez", "sara.gonzalez", "sara")
    );

    /**
     * Clase interna para almacenar información predefinida de usuarios.
     * Contiene todos los datos necesarios para crear un perfil básico de usuario.
     */
    public static class UsuarioInfo {
        public final String nombre;
        public final String apellidos;
        public final String username;
        public final String password;

        public UsuarioInfo(String nombre, String apellidos, String username, String password) {
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.username = username;
            this.password = password;
        }
    }
}