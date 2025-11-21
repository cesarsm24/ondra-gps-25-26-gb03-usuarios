package com.ondra.users.models.dao;

import com.ondra.users.models.enums.TipoRedSocial;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una red social asociada a un artista.
 *
 * <p>Almacena referencias de redes sociales vinculadas a un perfil de artista,
 * incluyendo el tipo de red social y la URL correspondiente.</p>
 */
@Entity
@Table(name = "redes_sociales")
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
     * Artista propietario de la red social.
     */
    @ManyToOne
    @JoinColumn(name = "id_artista", nullable = false)
    private Artista artista;

    /**
     * Tipo de red social.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRedSocial tipoRedSocial;

    /**
     * URL de la red social.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String urlRedSocial;
}