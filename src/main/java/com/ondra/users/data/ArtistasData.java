package com.ondra.users.data;

import java.util.Arrays;
import java.util.List;

/**
 * Clase que contiene los datos predefinidos de artistas para el seeding de la base de datos.
 * Incluye información completa de 6 artistas con nombre artístico, nombre real, biografía,
 * redes sociales y username.
 */
public class ArtistasData {

    /**
     * Lista de artistas predefinidos con su información completa.
     * Cada artista incluye nombre artístico, nombre real, biografía, redes sociales y username.
     */
    public static final List<ArtistaInfo> ARTISTAS_PREDEFINIDOS = Arrays.asList(
            new ArtistaInfo("Duki", "Mauro Ezequiel Lombardo Quiroga",
                    "Cantante, rapero y compositor argentino, considerado uno de los mayores exponentes del trap latino. Es conocido por éxitos como 'She Don't Give a FO', 'Goteo' y 'Givenchy'.",
                    Arrays.asList("INSTAGRAM", "X", "SPOTIFY", "YOUTUBE", "TIKTOK", "FACEBOOK"),
                    "duki", true),

            new ArtistaInfo("Aitana", "Aitana Ocaña Morales",
                    "Cantante española de pop. Salió de OT 2017 y tiene hits como '11 Razones', 'Akureyri' y 'Formentera'. Reconocida por su potente voz y su estilo versátil.",
                    Arrays.asList("INSTAGRAM", "X", "SPOTIFY", "YOUTUBE", "TIKTOK", "FACEBOOK"),
                    "aitana", true),

            new ArtistaInfo("Avicii", "Tim Bergling",
                    "DJ y productor sueco, leyenda de la música electrónica. Conocido mundialmente por 'Wake Me Up', 'Levels', 'Hey Brother' y 'The Nights'.",
                    Arrays.asList("INSTAGRAM", "X", "SPOTIFY", "YOUTUBE", "FACEBOOK"),
                    "avicii", false),

            new ArtistaInfo("Daddy Yankee", "Ramón Luis Ayala Rodríguez",
                    "El Rey del Reggaetón. Artista puertorriqueño pionero del género urbano. Con éxitos mundiales como 'Gasolina'. Leyenda viva de la música latina.",
                    Arrays.asList("INSTAGRAM", "X", "SPOTIFY", "YOUTUBE", "FACEBOOK", "TIKTOK"),
                    "daddyyankee", true),

            new ArtistaInfo("Rosalía", "Rosalía Vila Tobella",
                    "Artista catalana que fusiona flamenco con pop urbano contemporáneo. Ganadora de múltiples Grammy con álbumes como 'El Mal Querer' y 'Motomami'.",
                    Arrays.asList("INSTAGRAM", "X", "SPOTIFY", "YOUTUBE", "TIKTOK"),
                    "rosalia", true),

            new ArtistaInfo("Sanguijuelas del Guadiana", "Sanguijuelas del Guadiana",
                    "Banda extremeña que fusiona rock alternativo, blues y poesía urbana con letras crudas e ironía social, reflejando la esencia del Guadiana y la energía del panorama indie español.",
                    Arrays.asList("INSTAGRAM", "X", "SPOTIFY", "YOUTUBE", "TIKTOK"),
                    "sanguijuelasdelguadiana", true)
    );

    /**
     * Clase interna para almacenar información predefinida de artistas.
     * Contiene todos los datos necesarios para crear un perfil básico de artista.
     */
    public static class ArtistaInfo {
        public final String nombreArtistico;
        public final String nombreReal;
        public final String biografia;
        public final List<String> redesSociales;
        public final String username;
        public final boolean esTendencia;

        public ArtistaInfo(String nombreArtistico, String nombreReal, String biografia,
                           List<String> redesSociales, String username, boolean esTendencia) {
            this.nombreArtistico = nombreArtistico;
            this.nombreReal = nombreReal;
            this.biografia = biografia;
            this.redesSociales = redesSociales;
            this.username = username;
            this.esTendencia = esTendencia;
        }
    }
}