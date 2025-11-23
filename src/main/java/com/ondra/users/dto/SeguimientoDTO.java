package com.ondra.users.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO que representa un seguimiento entre dos usuarios.
 *
 * Incluye información del seguidor, del seguido y la fecha del seguimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoDTO {

    /** Identificador único del seguimiento. */
    private Long idSeguimiento;

    /** Información básica del usuario que sigue. */
    private UsuarioBasicoDTO seguidor;

    /** Información básica del usuario seguido. */
    private UsuarioBasicoDTO seguido;

    /** Fecha y hora en que se realizó el seguimiento. */
    private LocalDateTime fechaSeguimiento;
}
