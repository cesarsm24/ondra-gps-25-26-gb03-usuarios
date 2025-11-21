package com.ondra.users.models.dao;

import com.ondra.users.converters.EncryptedStringConverter;
import com.ondra.users.models.enums.TipoMetodoPago;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un método de cobro de un artista.
 *
 * <p>Soporta los tipos PAYPAL, BIZUM y TRANSFERENCIA.
 * Los datos sensibles se cifran automáticamente.</p>
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
     * Tipo de método de cobro.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMetodoPago tipoCobro;

    /**
     * Nombre del titular del método de cobro.
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
     * Provincia asociada al método de cobro.
     */
    @Column(nullable = false, length = 100)
    private String provincia;

    /**
     * Código postal asociado al método de cobro.
     */
    @Column(nullable = false, length = 10)
    private String codigoPostal;

    /**
     * Email de PayPal cifrado.
     *
     * <p>Campo específico para tipo PAYPAL.</p>
     */
    @Column(length = 500)
    @Convert(converter = EncryptedStringConverter.class)
    private String emailPaypal;

    /**
     * Número de teléfono de Bizum cifrado.
     *
     * <p>Campo específico para tipo BIZUM.</p>
     */
    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String telefonoBizum;

    /**
     * IBAN para transferencias cifrado.
     *
     * <p>Campo específico para tipo TRANSFERENCIA.</p>
     */
    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String iban;

    /**
     * Fecha de creación del registro.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización del registro.
     */
    @Column
    private LocalDateTime fechaActualizacion;

    /**
     * Inicializa las fechas de auditoría antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de modificación antes de actualizar.
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}