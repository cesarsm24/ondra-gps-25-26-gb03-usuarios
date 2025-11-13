package com.ondra.users.dto;

import com.ondra.users.dto.AlbumFavoritoDTO;
import com.ondra.users.dto.CancionFavoritaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa el resumen de favoritos de un usuario.
 * Recibido del microservicio de Contenidos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritosResumenDTO {
    private Integer totalFavoritos;
    private List<CancionFavoritaDTO> canciones;
    private List<AlbumFavoritoDTO> albumes;
}