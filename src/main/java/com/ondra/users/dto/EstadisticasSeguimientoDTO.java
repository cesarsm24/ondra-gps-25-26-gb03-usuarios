package com.ondra.users.dto;

import lombok.*;

/**
 * DTO que representa las estadísticas de seguimiento de un usuario.
 *
 * Contiene el número de usuarios seguidos y seguidores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasSeguimientoDTO {

    /** Identificador del usuario al que corresponden las estadísticas. */
    private Long idUsuario;

    /** Número total de usuarios que sigue este usuario. */
    private long seguidos;

    /** Número total de seguidores de este usuario. */
    private long seguidores;
}
