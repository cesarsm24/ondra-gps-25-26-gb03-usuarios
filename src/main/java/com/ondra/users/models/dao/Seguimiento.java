package com.ondra.users.models.dao;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la relación de seguimiento entre usuarios.
 *
 * <p>Define las restricciones de negocio para los seguimientos: usuarios normales pueden seguir
 * a artistas y otros usuarios normales, artistas no pueden seguir a nadie, se impide el auto-seguimiento
 * y se evitan seguimientos duplicados mediante restricción de unicidad.</p>
 */
@Entity
@Table(
        name = "seguimientos",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"id_seguidor", "id_seguido"},
                name = "uk_seguimiento_unico"
        ),
        indexes = {
                @Index(name = "idx_seguidor", columnList = "id_seguidor"),
                @Index(name = "idx_seguido", columnList = "id_seguido"),
                @Index(name = "idx_fecha_seguimiento", columnList = "fecha_seguimiento")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguimiento {

    /**
     * Identificador único del seguimiento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSeguimiento;

    /**
     * Usuario que realiza el seguimiento (follower).
     * Debe ser de tipo usuario normal. Los artistas no pueden seguir a otros usuarios.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seguidor", nullable = false)
    private Usuario seguidor;

    /**
     * Usuario que es seguido (following).
     * Puede ser un usuario normal o artista.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seguido", nullable = false)
    private Usuario seguido;

    /**
     * Fecha y hora en que se realizó el seguimiento.
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaSeguimiento = LocalDateTime.now();
}