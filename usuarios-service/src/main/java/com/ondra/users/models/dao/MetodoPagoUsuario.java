package com.ondra.users.models.dao;

import com.ondra.users.models.enums.TipoMetodoPago;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un método de pago de un usuario.
 *
 * <p>Almacena información de diferentes tipos de métodos de pago (TARJETA, PAYPAL, BIZUM, TRANSFERENCIA).
 * Incluye campos comunes para todos los tipos y campos específicos opcionales según el tipo seleccionado.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "metodos_pago_usuario")
public class MetodoPagoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMetodoPagoUsuario;

    /**
     * Usuario propietario del método de pago.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Tipo de método de pago (TARJETA, PAYPAL, BIZUM, TRANSFERENCIA).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMetodoPago tipoPago;

    // ==================== CAMPOS COMUNES ====================

    /**
     * Nombre del titular o propietario del método de pago.
     */
    @Column(nullable = false, length = 200)
    private String propietario;

    /**
     * Dirección de facturación asociada al método de pago.
     */
    @Column(nullable = false, length = 300)
    private String direccion;

    /**
     * País de facturación.
     */
    @Column(nullable = false, length = 100)
    private String pais;

    /**
     * Provincia o región de facturación.
     */
    @Column(nullable = false, length = 100)
    private String provincia;

    /**
     * Código postal de facturación.
     */
    @Column(nullable = false, length = 10)
    private String codigoPostal;

    // ==================== CAMPOS ESPECÍFICOS (OPCIONALES) ====================

    /**
     * Número de tarjeta de crédito o débito.
     * Campo específico para tipo TARJETA.
     */
    @Column(length = 19)
    private String numeroTarjeta;

    /**
     * Fecha de caducidad de la tarjeta en formato MM/YY.
     * Campo específico para tipo TARJETA.
     */
    @Column(length = 5)
    private String fechaCaducidad;

    /**
     * Código de seguridad CVV de la tarjeta.
     * Campo específico para tipo TARJETA.
     */
    @Column(length = 4)
    private String cvv;

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