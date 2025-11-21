package com.ondra.users.models.dao;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa el perfil artístico de un usuario.
 *
 * <p>Almacena información profesional del artista, incluyendo biografía,
 * foto de perfil y estado de tendencia.</p>
 */
@Entity
@Table(name = "Artistas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artista {

    /**
     * Identificador único del artista.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idArtista;

    /**
     * Usuario asociado al artista.
     *
     * <p>Relación uno a uno obligatoria y única.</p>
     */
    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    /**
     * Nombre artístico del usuario.
     */
    @Column(nullable = false)
    private String nombreArtistico;

    /**
     * Biografía del artista.
     */
    @Column(columnDefinition = "TEXT")
    private String biografiaArtistico;

    /**
     * URL de la foto de perfil artístico.
     */
    @Builder.Default
    private String fotoPerfilArtistico = null;

    /**
     * Fecha de creación del perfil artístico.
     */
    @Column(nullable = false)
    private LocalDateTime fechaInicioArtistico = LocalDateTime.now();

    /**
     * Indica si el artista está marcado como tendencia.
     */
    @Column(nullable = false)
    private boolean esTendencia = false;

    /**
     * Identificador único legible en URLs.
     */
    @Column(unique = true, length = 100)
    private String slugArtistico;
}