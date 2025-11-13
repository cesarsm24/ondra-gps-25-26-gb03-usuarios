package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa un álbum comprado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AlbumCompradoDTO {
    private Long idVenta;
    private Long idAlbum;
    private String tituloAlbum;
    private Long idArtista;
    private String nombreArtista;
    private String urlPortada;
    private Double precioPagado;
    private Integer totalCanciones;
    private LocalDateTime fechaCompra;
}