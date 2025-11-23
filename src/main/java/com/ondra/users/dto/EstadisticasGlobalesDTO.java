package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa estadísticas generales del sistema de usuarios.
 *
 * Incluye el total de usuarios registrados y el total de artistas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasGlobalesDTO {

    /** Total de usuarios registrados en el sistema. */
    private Long totalUsuarios;

    /** Total de usuarios que son artistas. */
    private Long totalArtistas;
}
