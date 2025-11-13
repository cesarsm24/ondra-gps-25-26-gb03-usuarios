package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa el resumen de compras de un usuario.
 * Recibido del microservicio de Contenidos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprasResumenDTO {
    private Integer totalCompras;
    private Double totalGastado;
    private List<CancionCompradaDTO> canciones;
    private List<AlbumCompradoDTO> albumes;
}