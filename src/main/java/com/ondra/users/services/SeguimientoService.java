package com.ondra.users.services;

import com.ondra.users.dto.EstadisticasSeguimientoDTO;
import com.ondra.users.dto.SeguimientoDTO;
import com.ondra.users.dto.UsuarioBasicoDTO;
import com.ondra.users.exceptions.*;
import com.ondra.users.models.dao.Artista;
import com.ondra.users.models.dao.Seguimiento;
import com.ondra.users.models.dao.Usuario;
import com.ondra.users.models.enums.TipoUsuario;
import com.ondra.users.repositories.SeguimientoRepository;
import com.ondra.users.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de seguimientos entre usuarios.
 *
 * <p>Implementa la funcionalidad de red social permitiendo a usuarios normales
 * seguir a otros usuarios y artistas.</p>
 *
 * <p>Reglas de negocio:</p>
 * <ul>
 *   <li>Solo usuarios con tipo NORMAL pueden seguir a otros</li>
 *   <li>Los artistas no pueden seguir, solo ser seguidos</li>
 *   <li>No se permite seguirse a uno mismo</li>
 *   <li>No se permiten seguimientos duplicados</li>
 *   <li>Solo se puede seguir a usuarios activos</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Crea un seguimiento entre dos usuarios.
     *
     * @param idSeguidor ID del usuario que sigue
     * @param idSeguido ID del usuario a seguir
     * @param authenticatedUserId ID del usuario autenticado
     * @return Información del seguimiento creado
     * @throws ForbiddenAccessException Si intenta seguir desde otra cuenta
     * @throws InvalidFollowException Si intenta seguirse a sí mismo o es artista
     * @throws UsuarioNotFoundException Si alguno de los usuarios no existe
     * @throws AccountInactiveException Si el usuario a seguir está inactivo
     * @throws DuplicateFollowException Si ya existe el seguimiento
     */
    @Transactional
    public SeguimientoDTO seguirUsuario(
            Long idSeguidor,
            Long idSeguido,
            Long authenticatedUserId
    ) {
        if (!idSeguidor.equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó crear seguimiento para usuario ID: {}",
                    authenticatedUserId, idSeguidor);
            throw new ForbiddenAccessException(
                    "Solo puedes seguir usuarios desde tu propia cuenta"
            );
        }

        if (idSeguidor.equals(idSeguido)) {
            log.warn("Usuario ID: {} intentó seguirse a sí mismo", idSeguidor);
            throw new InvalidFollowException("No puedes seguirte a ti mismo");
        }

        Usuario seguidor = usuarioRepository.findById(idSeguidor)
                .orElseThrow(() -> new UsuarioNotFoundException(idSeguidor));

        Usuario seguido = usuarioRepository.findById(idSeguido)
                .orElseThrow(() -> new UsuarioNotFoundException(idSeguido));

        if (seguidor.getTipoUsuario() != TipoUsuario.NORMAL) {
            log.warn("Artista ID: {} intentó seguir a alguien", idSeguidor);
            throw new InvalidFollowException(
                    "Los artistas no pueden seguir a otros usuarios"
            );
        }

        if (!seguido.isActivo()) {
            log.warn("Intento de seguir a usuario inactivo ID: {}", idSeguido);
            throw new AccountInactiveException("No puedes seguir a este usuario");
        }

        if (seguimientoRepository.existsBySeguidorIdUsuarioAndSeguidoIdUsuario(
                idSeguidor, idSeguido)) {
            log.warn("Usuario ID: {} ya sigue a usuario ID: {}", idSeguidor, idSeguido);
            throw new DuplicateFollowException("Ya sigues a este usuario");
        }

        Seguimiento seguimiento = Seguimiento.builder()
                .seguidor(seguidor)
                .seguido(seguido)
                .build();

        seguimiento = seguimientoRepository.save(seguimiento);

        log.info("➕ Seguimiento creado: Usuario ID: {} sigue a usuario ID: {}",
                idSeguidor, idSeguido);

        return convertirASeguimientoDTO(seguimiento);
    }

    /**
     * Elimina un seguimiento existente.
     *
     * @param idSeguidor ID del usuario que sigue
     * @param idSeguido ID del usuario seguido
     * @param authenticatedUserId ID del usuario autenticado
     * @throws ForbiddenAccessException Si intenta dejar de seguir desde otra cuenta
     * @throws FollowNotFoundException Si no existe el seguimiento
     */
    @Transactional
    public void dejarDeSeguir(
            Long idSeguidor,
            Long idSeguido,
            Long authenticatedUserId
    ) {
        if (!idSeguidor.equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó eliminar seguimiento para usuario ID: {}",
                    authenticatedUserId, idSeguidor);
            throw new ForbiddenAccessException(
                    "Solo puedes dejar de seguir desde tu propia cuenta"
            );
        }

        if (!seguimientoRepository.existsBySeguidorIdUsuarioAndSeguidoIdUsuario(
                idSeguidor, idSeguido)) {
            log.warn("Usuario ID: {} intentó dejar de seguir a usuario ID: {} pero no lo seguía",
                    idSeguidor, idSeguido);
            throw new FollowNotFoundException("No sigues a este usuario");
        }

        seguimientoRepository.deleteBySeguidorIdUsuarioAndSeguidoIdUsuario(
                idSeguidor, idSeguido
        );

        log.info("➖ Seguimiento eliminado: Usuario ID: {} dejó de seguir a usuario ID: {}",
                idSeguidor, idSeguido);
    }

    /**
     * Obtiene la lista de usuarios seguidos por un usuario.
     *
     * @param idUsuario ID del usuario
     * @return Lista de usuarios seguidos
     * @throws UsuarioNotFoundException Si el usuario no existe
     */
    @Transactional(readOnly = true)
    public List<UsuarioBasicoDTO> obtenerSeguidos(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException(idUsuario));

        List<Seguimiento> seguimientos = seguimientoRepository
                .findBySeguidorIdUsuario(idUsuario);

        return seguimientos.stream()
                .map(s -> {
                    Usuario seguido = s.getSeguido();

                    if (seguido.getTipoUsuario() == TipoUsuario.ARTISTA) {
                        Artista artista = seguido.getArtista();
                        if (artista != null) {
                            artista.getNombreArtistico();
                        }
                    }
                    return convertirAUsuarioBasicoDTO(seguido);
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de seguidores de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return Lista de seguidores
     * @throws UsuarioNotFoundException Si el usuario no existe
     */
    @Transactional(readOnly = true)
    public List<UsuarioBasicoDTO> obtenerSeguidores(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException(idUsuario));

        List<Seguimiento> seguimientos = seguimientoRepository
                .findBySeguidoIdUsuario(idUsuario);

        return seguimientos.stream()
                .map(s -> {
                    Usuario seguidor = s.getSeguidor();

                    if (seguidor.getTipoUsuario() == TipoUsuario.ARTISTA) {
                        Artista artista = seguidor.getArtista();
                        if (artista != null) {
                            artista.getNombreArtistico();
                        }
                    }
                    return convertirAUsuarioBasicoDTO(seguidor);
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las estadísticas de seguimientos de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return Contadores de seguidos y seguidores
     * @throws UsuarioNotFoundException Si el usuario no existe
     */
    @Transactional(readOnly = true)
    public EstadisticasSeguimientoDTO obtenerEstadisticas(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException(idUsuario));

        long cantidadSeguidos = seguimientoRepository
                .countBySeguidorIdUsuario(idUsuario);
        long cantidadSeguidores = seguimientoRepository
                .countBySeguidoIdUsuario(idUsuario);

        return EstadisticasSeguimientoDTO.builder()
                .idUsuario(idUsuario)
                .seguidos(cantidadSeguidos)
                .seguidores(cantidadSeguidores)
                .build();
    }

    /**
     * Verifica si existe un seguimiento entre dos usuarios.
     *
     * @param idSeguidor ID del usuario seguidor
     * @param idSeguido ID del usuario seguido
     * @return true si el seguimiento existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean verificarSeguimiento(Long idSeguidor, Long idSeguido) {
        return seguimientoRepository.existsBySeguidorIdUsuarioAndSeguidoIdUsuario(
                idSeguidor, idSeguido
        );
    }

    /**
     * Convierte una entidad Seguimiento a DTO.
     *
     * @param seguimiento Entidad a convertir
     * @return DTO correspondiente
     */
    private SeguimientoDTO convertirASeguimientoDTO(Seguimiento seguimiento) {
        return SeguimientoDTO.builder()
                .idSeguimiento(seguimiento.getIdSeguimiento())
                .seguidor(convertirAUsuarioBasicoDTO(seguimiento.getSeguidor()))
                .seguido(convertirAUsuarioBasicoDTO(seguimiento.getSeguido()))
                .fechaSeguimiento(seguimiento.getFechaSeguimiento())
                .build();
    }

    /**
     * Convierte una entidad Usuario a DTO básico.
     *
     * @param usuario Entidad a convertir
     * @return DTO con información básica del usuario
     */
    private UsuarioBasicoDTO convertirAUsuarioBasicoDTO(Usuario usuario) {
        UsuarioBasicoDTO.UsuarioBasicoDTOBuilder builder = UsuarioBasicoDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .apellidosUsuario(usuario.getApellidosUsuario())
                .fotoPerfil(usuario.getFotoPerfil())
                .tipoUsuario(usuario.getTipoUsuario())
                .slug(usuario.getSlug());

        if (usuario.getTipoUsuario() == TipoUsuario.ARTISTA && usuario.getArtista() != null) {
            builder.nombreArtistico(usuario.getArtista().getNombreArtistico())
                    .slugArtistico(usuario.getArtista().getSlugArtistico());
        }

        return builder.build();
    }
}