package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa un álbum favorito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AlbumFavoritoDTO {
    private Long idFavorito;
    private Long idAlbum;
    private String tituloAlbum;
    private Long idArtista;
    private String nombreArtista;
    private String urlPortada;
    private LocalDateTime fechaAgregado;
}
