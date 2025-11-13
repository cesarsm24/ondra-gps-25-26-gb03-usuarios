package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa una canción comprada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CancionCompradaDTO {
    private Long idVenta;
    private Long idCancion;
    private String tituloCancion;
    private Long idArtista;
    private String nombreArtista;
    private String urlPortada;
    private Double precioPagado;
    private LocalDateTime fechaCompra;
}
