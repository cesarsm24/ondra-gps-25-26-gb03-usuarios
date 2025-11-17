package com.ondra.users.models.dao;

import com.ondra.users.models.enums.TipoMetodoPago;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un método de cobro de un artista.
 *
 * <p>Almacena información de diferentes tipos de métodos de cobro permitidos para artistas
 * (PAYPAL, BIZUM, TRANSFERENCIA). El tipo TARJETA está excluido para este perfil.
 * Incluye campos comunes y campos específicos opcionales según el tipo seleccionado.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "metodos_cobro_artista")
public class MetodoCobroArtista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMetodoCobroArtista;

    /**
     * Artista propietario del método de cobro.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_artista", nullable = false)
    private Artista artista;

    /**
     * Tipo de método de cobro (PAYPAL, BIZUM, TRANSFERENCIA).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMetodoPago tipoCobro;

    // ==================== CAMPOS COMUNES ====================

    /**
     * Nombre del titular o propietario del método de cobro.
     */
    @Column(nullable = false, length = 200)
    private String propietario;

    /**
     * Dirección asociada al método de cobro.
     */
    @Column(nullable = false, length = 300)
    private String direccion;

    /**
     * País asociado al método de cobro.
     */
    @Column(nullable = false, length = 100)
    private String pais;

    /**
     * Provincia o región asociada al método de cobro.
     */
    @Column(nullable = false, length = 100)
    private String provincia;

    /**
     * Código postal asociado al método de cobro.
     */
    @Column(nullable = false, length = 10)
    private String codigoPostal;

    // ==================== CAMPOS ESPECÍFICOS (OPCIONALES) ====================

    /**
     * Dirección de correo electrónico asociada a PayPal.
     * Campo específico para tipo PAYPAL.
     */
    @Column(length = 255)
    private String emailPaypal;

    /**
     * Número de teléfono asociado a Bizum.
     * Campo específico para tipo BIZUM.
     */
    @Column(length = 20)
    private String telefonoBizum;

    /**
     * Código IBAN para transferencias bancarias.
     * Campo específico para tipo TRANSFERENCIA.
     */
    @Column(length = 34)
    private String iban;

    // ==================== AUDITORÍA ====================

    /**
     * Fecha y hora de creación del registro.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última actualización del registro.
     */
    @Column
    private LocalDateTime fechaActualizacion;

    /**
     * Callback ejecutado antes de persistir la entidad.
     * Inicializa las fechas de auditoría.
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Callback ejecutado antes de actualizar la entidad.
     * Actualiza la fecha de modificación.
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}