package com.ondra.users.models.dao;

import com.ondra.users.models.enums.TipoRedSocial;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa una red social asociada a un artista.
 *
 * <p>Incluye información sobre el artista, tipo de red social y URL correspondiente.</p>
 */
@Entity
@Table(name = "RedesSociales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedSocial {

    /**
     * Identificador único de la red social.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRedSocial;

    /**
     * Artista asociado a la red social.
     *
     * <p>Relación muchos a uno. No puede ser nulo.</p>
     */
    @ManyToOne
    @JoinColumn(name = "id_artista", nullable = false)
    private Artista artista;

    /**
     * Tipo de red social.
     *
     * <p>Se almacena como {@link TipoRedSocial} (ENUM) y es obligatorio.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRedSocial tipoRedSocial;

    /**
     * URL de la red social.
     *
     * <p>Campo obligatorio y almacenado como texto largo.</p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String urlRedSocial;
}