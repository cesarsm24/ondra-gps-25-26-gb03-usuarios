package com.ondra.users.services;

import com.ondra.users.dto.MetodoPagoUsuarioCrearDTO;
import com.ondra.users.dto.MetodoPagoUsuarioDTO;
import com.ondra.users.dto.MetodoPagoUsuarioEditarDTO;
import com.ondra.users.exceptions.ForbiddenAccessException;
import com.ondra.users.exceptions.InvalidPaymentMethodException;
import com.ondra.users.exceptions.MetodoPagoUsuarioNotFoundException;
import com.ondra.users.exceptions.UsuarioNotFoundException;
import com.ondra.users.models.dao.MetodoPagoUsuario;
import com.ondra.users.models.enums.TipoMetodoPago;
import com.ondra.users.repositories.MetodoPagoUsuarioRepository;
import com.ondra.users.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetodoPagoUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoUsuarioRepository metodoPagoRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ==================== LISTAR ====================
    @Transactional(readOnly = true)
    public List<MetodoPagoUsuarioDTO> listarMetodosPago(Long idUsuario, Long authenticatedUserId) {
        if (!idUsuario.equals(authenticatedUserId)) {
            throw new ForbiddenAccessException("No tienes permiso para ver los métodos de pago de este usuario");
        }

        var usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException(idUsuario));

        return metodoPagoRepository.findByUsuario(usuario)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ==================== CREAR ====================
    @Transactional
    public MetodoPagoUsuarioDTO crearMetodoPago(Long idUsuario, MetodoPagoUsuarioCrearDTO crearDTO, Long authenticatedUserId) {
        if (!idUsuario.equals(authenticatedUserId)) {
            throw new ForbiddenAccessException("No tienes permiso para añadir métodos de pago a este usuario");
        }

        var usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException(idUsuario));

        TipoMetodoPago tipoMetodoPago;
        try {
            tipoMetodoPago = TipoMetodoPago.valueOf(crearDTO.getMetodoPago().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidPaymentMethodException("Método de pago no válido: " + crearDTO.getMetodoPago());
        }

        // Validar campos específicos según el método
        validarCamposEspecificos(tipoMetodoPago, crearDTO);

        MetodoPagoUsuario metodoPagoUsuario = MetodoPagoUsuario.builder()
                .usuario(usuario)
                .tipoPago(tipoMetodoPago)
                .propietario(crearDTO.getPropietario())
                .direccion(crearDTO.getDireccion())
                .pais(crearDTO.getPais())
                .provincia(crearDTO.getProvincia())
                .codigoPostal(crearDTO.getCodigoPostal())
                .build();

        // Asignar campos específicos según el tipo
        asignarCamposEspecificos(metodoPagoUsuario, tipoMetodoPago, crearDTO);

        var metodoPagoGuardado = metodoPagoRepository.save(metodoPagoUsuario);
        log.info("✅ Método de pago creado con ID: {}", metodoPagoGuardado.getIdMetodoPagoUsuario());

        return convertirADTO(metodoPagoGuardado);
    }

    // ==================== EDITAR ====================
    @Transactional
    public MetodoPagoUsuarioDTO editarMetodoPago(Long idUsuario, Long idMetodoPago, MetodoPagoUsuarioEditarDTO editarDTO, Long authenticatedUserId) {
        if (!idUsuario.equals(authenticatedUserId)) {
            throw new ForbiddenAccessException("No tienes permiso para modificar métodos de pago de este usuario");
        }

        var usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException(idUsuario));

        var metodoPago = metodoPagoRepository.findByIdMetodoPagoUsuarioAndUsuario(idMetodoPago, usuario)
                .orElseThrow(() -> new MetodoPagoUsuarioNotFoundException(idMetodoPago));

        // Actualizar solo los campos que no sean null
        if (editarDTO.getPropietario() != null) {
            metodoPago.setPropietario(editarDTO.getPropietario());
        }

        // Actualizar campos específicos según el tipo
        actualizarCamposEspecificos(metodoPago, editarDTO);

        var metodoPagoActualizado = metodoPagoRepository.save(metodoPago);
        log.info("✅ Método de pago actualizado con ID: {}", metodoPagoActualizado.getIdMetodoPagoUsuario());

        return convertirADTO(metodoPagoActualizado);
    }

    // ==================== ELIMINAR ====================
    @Transactional
    public void eliminarMetodoPago(Long idUsuario, Long idMetodoPago, Long authenticatedUserId) {
        if (!idUsuario.equals(authenticatedUserId)) {
            throw new ForbiddenAccessException("No tienes permiso para eliminar este método de pago");
        }

        var usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException(idUsuario));

        var metodoPago = metodoPagoRepository.findByIdMetodoPagoUsuarioAndUsuario(idMetodoPago, usuario)
                .orElseThrow(() -> new MetodoPagoUsuarioNotFoundException(idMetodoPago));

        metodoPagoRepository.delete(metodoPago);
        log.info("✅ Método de pago eliminado con ID: {}", idMetodoPago);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void validarCamposEspecificos(TipoMetodoPago tipoMetodoPago, MetodoPagoUsuarioCrearDTO dto) {
        switch (tipoMetodoPago) {
            case TARJETA:
                if (dto.getNumeroTarjeta() == null || dto.getFechaCaducidad() == null || dto.getCvv() == null) {
                    throw new InvalidPaymentMethodException("Tarjeta requiere: numeroTarjeta, fechaCaducidad y cvv");
                }
                break;
            case PAYPAL:
                if (dto.getEmailPaypal() == null) {
                    throw new InvalidPaymentMethodException("PayPal requiere: emailPaypal");
                }
                break;
            case BIZUM:
                if (dto.getTelefonoBizum() == null) {
                    throw new InvalidPaymentMethodException("Bizum requiere: telefonoBizum");
                }
                break;
            case TRANSFERENCIA:
                if (dto.getIban() == null) {
                    throw new InvalidPaymentMethodException("Transferencia requiere: iban");
                }
                break;
        }
    }

    private void asignarCamposEspecificos(MetodoPagoUsuario metodoPago, TipoMetodoPago tipo, MetodoPagoUsuarioCrearDTO dto) {
        switch (tipo) {
            case TARJETA:
                metodoPago.setNumeroTarjeta(dto.getNumeroTarjeta());
                metodoPago.setFechaCaducidad(dto.getFechaCaducidad());
                metodoPago.setCvv(dto.getCvv());
                break;
            case PAYPAL:
                metodoPago.setEmailPaypal(dto.getEmailPaypal());
                break;
            case BIZUM:
                metodoPago.setTelefonoBizum(dto.getTelefonoBizum());
                break;
            case TRANSFERENCIA:
                metodoPago.setIban(dto.getIban());
                break;
        }
    }

    private void actualizarCamposEspecificos(MetodoPagoUsuario metodoPago, MetodoPagoUsuarioEditarDTO dto) {
        switch (metodoPago.getTipoPago()) {
            case TARJETA:
                if (dto.getNumeroTarjeta() != null) metodoPago.setNumeroTarjeta(dto.getNumeroTarjeta());
                if (dto.getFechaCaducidad() != null) metodoPago.setFechaCaducidad(dto.getFechaCaducidad());
                if (dto.getCvv() != null) metodoPago.setCvv(dto.getCvv());
                break;
            case PAYPAL:
                if (dto.getEmailPaypal() != null) metodoPago.setEmailPaypal(dto.getEmailPaypal());
                break;
            case BIZUM:
                if (dto.getTelefonoBizum() != null) metodoPago.setTelefonoBizum(dto.getTelefonoBizum());
                break;
            case TRANSFERENCIA:
                if (dto.getIban() != null) metodoPago.setIban(dto.getIban());
                break;
        }
    }

    private MetodoPagoUsuarioDTO convertirADTO(MetodoPagoUsuario metodoPago) {
        return MetodoPagoUsuarioDTO.builder()
                .idMetodoPago(metodoPago.getIdMetodoPagoUsuario())
                .tipo(metodoPago.getTipoPago().name().toLowerCase())
                .propietario(metodoPago.getPropietario())
                .direccion(metodoPago.getDireccion())
                .pais(metodoPago.getPais())
                .provincia(metodoPago.getProvincia())
                .codigoPostal(metodoPago.getCodigoPostal())
                .numeroTarjeta(metodoPago.getNumeroTarjeta())
                .fechaCaducidad(metodoPago.getFechaCaducidad())
                .cvv(metodoPago.getCvv())
                .emailPaypal(metodoPago.getEmailPaypal())
                .telefonoBizum(metodoPago.getTelefonoBizum())
                .iban(metodoPago.getIban())
                .fechaCreacion(metodoPago.getFechaCreacion().format(FORMATTER))
                .fechaActualizacion(metodoPago.getFechaActualizacion().format(FORMATTER))
                .build();
    }
}