package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa una canción favorita.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CancionFavoritaDTO {
    private Long idFavorito;
    private Long idCancion;
    private String tituloCancion;
    private Long idArtista;
    private String nombreArtista;
    private String urlPortada;
    private LocalDateTime fechaAgregado;
}