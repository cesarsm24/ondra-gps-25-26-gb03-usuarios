package com.ondra.users.repositories;

import com.ondra.users.models.dao.MetodoPagoUsuario;
import com.ondra.users.models.dao.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de métodos de pago de usuarios.
 *
 * <p>Proporciona operaciones de acceso a datos para métodos de pago,
 * incluyendo consultas filtradas por usuario y validación de propiedad.</p>
 */
@Repository
public interface MetodoPagoUsuarioRepository extends JpaRepository<MetodoPagoUsuario, Long> {

    /**
     * Busca todos los métodos de pago asociados a un usuario.
     *
     * @param usuario entidad del usuario
     * @return lista de métodos de pago del usuario
     */
    List<MetodoPagoUsuario> findByUsuario(Usuario usuario);

    /**
     * Busca un método de pago específico verificando que pertenezca al usuario.
     *
     * @param id identificador del método de pago
     * @param usuario entidad del usuario propietario
     * @return Optional con el método de pago si existe y pertenece al usuario
     */
    Optional<MetodoPagoUsuario> findByIdMetodoPagoUsuarioAndUsuario(Long id, Usuario usuario);

    /**
     * Elimina un método de pago verificando que pertenezca al usuario.
     *
     * @param id identificador del método de pago
     * @param usuario entidad del usuario propietario
     */
    void deleteByIdMetodoPagoUsuarioAndUsuario(Long id, Usuario usuario);

    /**
     * Busca todos los métodos de pago de un usuario por su identificador.
     *
     * @param idUsuario identificador del usuario
     * @return lista de métodos de pago del usuario
     */
    List<MetodoPagoUsuario> findByUsuario_IdUsuario(Long idUsuario);
}