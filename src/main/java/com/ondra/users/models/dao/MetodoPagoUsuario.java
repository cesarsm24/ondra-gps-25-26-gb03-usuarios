package com.ondra.users.models.dao;

import com.ondra.users.converters.EncryptedStringConverter;
import com.ondra.users.models.enums.TipoMetodoPago;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un método de pago asociado a un usuario.
 *
 * <p>Almacena información de métodos de pago de diferentes tipos: tarjeta de crédito o débito,
 * PayPal, Bizum y transferencia bancaria. Incluye campos comunes para todos los tipos y campos
 * específicos opcionales según el tipo seleccionado. La información sensible se cifra mediante
 * {@link EncryptedStringConverter}.</p>
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
     * Tipo de método de pago.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMetodoPago tipoPago;

    /**
     * Nombre del titular del método de pago.
     */
    @Column(nullable = false, length = 200)
    private String propietario;

    /**
     * Dirección de facturación.
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

    /**
     * Número de tarjeta cifrado. Específico para tipo de pago TARJETA.
     */
    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String numeroTarjeta;

    /**
     * Fecha de caducidad de la tarjeta en formato MM/YY cifrada. Específico para tipo de pago TARJETA.
     */
    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String fechaCaducidad;

    /**
     * Código de seguridad CVV de la tarjeta cifrado. Específico para tipo de pago TARJETA.
     */
    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String cvv;

    /**
     * Correo electrónico asociado a PayPal cifrado. Específico para tipo de pago PAYPAL.
     */
    @Column(length = 500)
    @Convert(converter = EncryptedStringConverter.class)
    private String emailPaypal;

    /**
     * Número de teléfono asociado a Bizum cifrado. Específico para tipo de pago BIZUM.
     */
    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String telefonoBizum;

    /**
     * Código IBAN para transferencias bancarias cifrado. Específico para tipo de pago TRANSFERENCIA.
     */
    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String iban;

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
     * Inicializa las fechas de auditoría al persistir la entidad.
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de modificación al actualizar la entidad.
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}