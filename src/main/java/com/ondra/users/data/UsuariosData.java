package com.ondra.users.data;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Proporciona datos predefinidos de usuarios y su información básica.
 * Clase utilitaria que no debe ser instanciada.
 */
public final class UsuariosData {

    /**
     * Constructor privado para evitar instanciación de clase utilitaria.
     */
    private UsuariosData() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Contiene la información principal de un usuario.
     */
    @Getter
    @AllArgsConstructor
    public static class UsuarioInfo {

        /**
         * Nombre del usuario.
         */
        private final String nombre;

        /**
         * Apellidos del usuario.
         */
        private final String apellidos;

        /**
         * Identificador único utilizado en la plataforma.
         */
        private final String username;

        /**
         * URL de la imagen asociada al usuario.
         */
        private final String urlImagenCompartida;
    }

    /**
     * Lista predefinida de usuarios con datos básicos.
     */
    public static final List<UsuarioInfo> USUARIOS_PREDEFINIDOS = List.of(
            new UsuarioInfo(
                    "Ana",
                    "García López",
                    "anagarcia",
                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764201947/anagarcia_ab2im5.jpg"
            ),
            new UsuarioInfo(
                    "Carlos",
                    "Martínez Ruiz",
                    "carlosmartinez",
                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764201949/carlosmartinez_vmpvwd.webp"
            ),
            new UsuarioInfo(
                    "Laura",
                    "Rodríguez Sánchez",
                    "laurarodriguez",
                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764201951/laurarodriguez_i6fh7k.jpg"
            ),
            new UsuarioInfo(
                    "Miguel",
                    "Fernández Torres",
                    "miguelfernandez",
                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764201952/miguelfernandez_ggngar.jpg"
            ),
            new UsuarioInfo(
                    "Sara",
                    "González Pérez",
                    "saragonzalez",
                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764201953/saragonzalez_tv5bgz.jpg"
            )
    );
}