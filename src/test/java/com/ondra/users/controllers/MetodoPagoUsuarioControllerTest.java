package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.dto.MetodoPagoUsuarioCrearDTO;
import com.ondra.users.dto.MetodoPagoUsuarioDTO;
import com.ondra.users.dto.MetodoPagoUsuarioEditarDTO;
import com.ondra.users.exceptions.ForbiddenAccessException;
import com.ondra.users.exceptions.MetodoPagoUsuarioNotFoundException;
import com.ondra.users.exceptions.UsuarioNotFoundException;
import com.ondra.users.services.MetodoPagoUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link MetodoPagoUsuarioController}.
 *
 * <p>Se verifican los endpoints relacionados con la gestión de métodos de pago de usuarios:
 * listar, crear, editar y eliminar métodos de pago.</p>
 *
 * <p>Se incluyen casos de éxito, errores, validaciones y restricciones de permisos.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class MetodoPagoUsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetodoPagoUsuarioService metodoPagoService;

    @Autowired
    private TestJwtHelper jwtHelper;

    private String tokenUsuario1;
    private String tokenUsuario2;

    @BeforeEach
    void setUp() {
        tokenUsuario1 = jwtHelper.generarTokenPrueba(1L, "juan@example.com");
        tokenUsuario2 = jwtHelper.generarTokenPrueba(2L, "otro@example.com");
    }

    // ========== TESTS DE LISTAR MÉTODOS DE PAGO ==========

    @Test
    void listarMetodosPago_Success() throws Exception {
        MetodoPagoUsuarioDTO metodo1 = MetodoPagoUsuarioDTO.builder()
                .idMetodoPago(1L)
                .tipo("tarjeta")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .numeroTarjeta("4532123456789012")
                .fechaCaducidad("12/25")
                .cvv("123")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        MetodoPagoUsuarioDTO metodo2 = MetodoPagoUsuarioDTO.builder()
                .idMetodoPago(2L)
                .tipo("paypal")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .emailPaypal("juan@paypal.com")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoPagoService.listarMetodosPago(eq(1L), eq(1L)))
                .thenReturn(Arrays.asList(metodo1, metodo2));

        mockMvc.perform(get("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idMetodoPago", is(1)))
                .andExpect(jsonPath("$[0].tipo", is("tarjeta")))
                .andExpect(jsonPath("$[1].idMetodoPago", is(2)))
                .andExpect(jsonPath("$[1].tipo", is("paypal")));

        verify(metodoPagoService, times(1)).listarMetodosPago(1L, 1L);
    }

    @Test
    void listarMetodosPago_ListaVacia() throws Exception {
        when(metodoPagoService.listarMetodosPago(eq(1L), eq(1L)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(metodoPagoService, times(1)).listarMetodosPago(1L, 1L);
    }

    @Test
    void listarMetodosPago_SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/usuarios/1/metodos-pago")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(metodoPagoService, never()).listarMetodosPago(any(), any());
    }

    @Test
    void listarMetodosPago_OtroUsuario_Forbidden() throws Exception {
        when(metodoPagoService.listarMetodosPago(eq(1L), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para ver los métodos de pago de este usuario"));

        mockMvc.perform(get("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));

        verify(metodoPagoService, times(1)).listarMetodosPago(1L, 2L);
    }

    @Test
    void listarMetodosPago_UsuarioNoExiste() throws Exception {
        when(metodoPagoService.listarMetodosPago(eq(999L), eq(999L)))
                .thenThrow(new UsuarioNotFoundException(999L));

        String tokenUsuario999 = jwtHelper.generarTokenPrueba(999L, "noexiste@example.com");

        mockMvc.perform(get("/api/usuarios/999/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("USER_NOT_FOUND")));

        verify(metodoPagoService, times(1)).listarMetodosPago(999L, 999L);
    }

    // ========== TESTS DE CREAR MÉTODOS DE PAGO ==========

    @Test
    void crearMetodoPagoTarjeta_Success() throws Exception {
        MetodoPagoUsuarioCrearDTO crearDTO = MetodoPagoUsuarioCrearDTO.builder()
                .metodoPago("TARJETA")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .numeroTarjeta("4532123456789012")
                .fechaCaducidad("12/25")
                .cvv("123")
                .build();

        MetodoPagoUsuarioDTO metodoCreado = MetodoPagoUsuarioDTO.builder()
                .idMetodoPago(1L)
                .tipo("tarjeta")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .numeroTarjeta("4532123456789012")
                .fechaCaducidad("12/25")
                .cvv("123")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoPagoService.crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L)))
                .thenReturn(metodoCreado);

        mockMvc.perform(post("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMetodoPago", is(1)))
                .andExpect(jsonPath("$.tipo", is("tarjeta")))
                .andExpect(jsonPath("$.numeroTarjeta", is("4532123456789012")));

        verify(metodoPagoService, times(1)).crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoPagoPaypal_Success() throws Exception {
        MetodoPagoUsuarioCrearDTO crearDTO = MetodoPagoUsuarioCrearDTO.builder()
                .metodoPago("PAYPAL")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .emailPaypal("juan@paypal.com")
                .build();

        MetodoPagoUsuarioDTO metodoCreado = MetodoPagoUsuarioDTO.builder()
                .idMetodoPago(2L)
                .tipo("paypal")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .emailPaypal("juan@paypal.com")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoPagoService.crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L)))
                .thenReturn(metodoCreado);

        mockMvc.perform(post("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMetodoPago", is(2)))
                .andExpect(jsonPath("$.tipo", is("paypal")))
                .andExpect(jsonPath("$.emailPaypal", is("juan@paypal.com")));

        verify(metodoPagoService, times(1)).crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoPagoBizum_Success() throws Exception {
        MetodoPagoUsuarioCrearDTO crearDTO = MetodoPagoUsuarioCrearDTO.builder()
                .metodoPago("BIZUM")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .telefonoBizum("+34612345678")
                .build();

        MetodoPagoUsuarioDTO metodoCreado = MetodoPagoUsuarioDTO.builder()
                .idMetodoPago(3L)
                .tipo("bizum")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .telefonoBizum("+34612345678")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoPagoService.crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L)))
                .thenReturn(metodoCreado);

        mockMvc.perform(post("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMetodoPago", is(3)))
                .andExpect(jsonPath("$.tipo", is("bizum")))
                .andExpect(jsonPath("$.telefonoBizum", is("+34612345678")));

        verify(metodoPagoService, times(1)).crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoPagoTransferencia_Success() throws Exception {
        MetodoPagoUsuarioCrearDTO crearDTO = MetodoPagoUsuarioCrearDTO.builder()
                .metodoPago("TRANSFERENCIA")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .iban("ES9121000418450200051332")
                .build();

        MetodoPagoUsuarioDTO metodoCreado = MetodoPagoUsuarioDTO.builder()
                .idMetodoPago(4L)
                .tipo("transferencia")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .iban("ES9121000418450200051332")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoPagoService.crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L)))
                .thenReturn(metodoCreado);

        mockMvc.perform(post("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMetodoPago", is(4)))
                .andExpect(jsonPath("$.tipo", is("transferencia")))
                .andExpect(jsonPath("$.iban", is("ES9121000418450200051332")));

        verify(metodoPagoService, times(1)).crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoPago_TipoInvalido() throws Exception {
        MetodoPagoUsuarioCrearDTO crearDTO = MetodoPagoUsuarioCrearDTO.builder()
                .metodoPago("CRIPTOMONEDA")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .build();

        mockMvc.perform(post("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));

        verify(metodoPagoService, never()).crearMetodoPago(any(), any(), any());
    }

    @Test
    void crearMetodoPagoTarjeta_CamposFaltantes() throws Exception {
        MetodoPagoUsuarioCrearDTO crearDTO = MetodoPagoUsuarioCrearDTO.builder()
                .metodoPago("TARJETA")
                .propietario("Juan Pérez")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .build();

        when(metodoPagoService.crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L)))
                .thenThrow(new com.ondra.users.exceptions.InvalidPaymentMethodException(
                        "Tarjeta requiere: numeroTarjeta, fechaCaducidad y cvv"));

        mockMvc.perform(post("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYMENT_METHOD")));

        verify(metodoPagoService, times(1)).crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoPago_OtroUsuario_Forbidden() throws Exception {
        MetodoPagoUsuarioCrearDTO crearDTO = MetodoPagoUsuarioCrearDTO.builder()
                .metodoPago("TARJETA")
                .propietario("Intruso")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .numeroTarjeta("4532123456789012")
                .fechaCaducidad("12/25")
                .cvv("123")
                .build();

        when(metodoPagoService.crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para añadir métodos de pago a este usuario"));

        mockMvc.perform(post("/api/usuarios/1/metodos-pago")
                        .header("Authorization", "Bearer " + tokenUsuario2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));

        verify(metodoPagoService, times(1)).crearMetodoPago(eq(1L), any(MetodoPagoUsuarioCrearDTO.class), eq(2L));
    }

    // ========== TESTS DE EDITAR MÉTODOS DE PAGO ==========

    @Test
    void editarMetodoPago_Success() throws Exception {
        MetodoPagoUsuarioEditarDTO editarDTO = MetodoPagoUsuarioEditarDTO.builder()
                .propietario("Juan Pérez Actualizado")
                .numeroTarjeta("4532987654321098")
                .build();

        MetodoPagoUsuarioDTO metodoActualizado = MetodoPagoUsuarioDTO.builder()
                .idMetodoPago(1L)
                .tipo("tarjeta")
                .propietario("Juan Pérez Actualizado")
                .direccion("Calle Falsa 123")
                .pais("España")
                .provincia("Madrid")
                .codigoPostal("28001")
                .numeroTarjeta("4532987654321098")
                .fechaCaducidad("12/25")
                .cvv("123")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("02/01/2024 15:00")
                .build();

        when(metodoPagoService.editarMetodoPago(eq(1L), eq(1L), any(MetodoPagoUsuarioEditarDTO.class), eq(1L)))
                .thenReturn(metodoActualizado);

        mockMvc.perform(put("/api/usuarios/1/metodos-pago/1")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propietario", is("Juan Pérez Actualizado")))
                .andExpect(jsonPath("$.numeroTarjeta", is("4532987654321098")));

        verify(metodoPagoService, times(1)).editarMetodoPago(eq(1L), eq(1L), any(MetodoPagoUsuarioEditarDTO.class), eq(1L));
    }

    @Test
    void editarMetodoPago_OtroUsuario_Forbidden() throws Exception {
        MetodoPagoUsuarioEditarDTO editarDTO = MetodoPagoUsuarioEditarDTO.builder()
                .propietario("Intruso")
                .build();

        when(metodoPagoService.editarMetodoPago(eq(1L), eq(1L), any(MetodoPagoUsuarioEditarDTO.class), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para modificar métodos de pago de este usuario"));

        mockMvc.perform(put("/api/usuarios/1/metodos-pago/1")
                        .header("Authorization", "Bearer " + tokenUsuario2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));

        verify(metodoPagoService, times(1)).editarMetodoPago(eq(1L), eq(1L), any(MetodoPagoUsuarioEditarDTO.class), eq(2L));
    }

    @Test
    void editarMetodoPago_NoExiste() throws Exception {
        MetodoPagoUsuarioEditarDTO editarDTO = MetodoPagoUsuarioEditarDTO.builder()
                .propietario("Juan Pérez")
                .build();

        when(metodoPagoService.editarMetodoPago(eq(1L), eq(999L), any(MetodoPagoUsuarioEditarDTO.class), eq(1L)))
                .thenThrow(new MetodoPagoUsuarioNotFoundException(999L));

        mockMvc.perform(put("/api/usuarios/1/metodos-pago/999")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("PAYMENT_NOT_FOUND")));

        verify(metodoPagoService, times(1)).editarMetodoPago(eq(1L), eq(999L), any(MetodoPagoUsuarioEditarDTO.class), eq(1L));
    }

    // ========== TESTS DE ELIMINAR MÉTODOS DE PAGO ==========

    @Test
    void eliminarMetodoPago_Success() throws Exception {
        doNothing().when(metodoPagoService).eliminarMetodoPago(eq(1L), eq(1L), eq(1L));

        mockMvc.perform(delete("/api/usuarios/1/metodos-pago/1")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Método de pago eliminado correctamente"));

        verify(metodoPagoService, times(1)).eliminarMetodoPago(1L, 1L, 1L);
    }

    @Test
    void eliminarMetodoPago_SinAutenticacion() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1/metodos-pago/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(metodoPagoService, never()).eliminarMetodoPago(any(), any(), any());
    }

    @Test
    void eliminarMetodoPago_OtroUsuario_Forbidden() throws Exception {
        doThrow(new ForbiddenAccessException("No tienes permiso para eliminar este método de pago"))
                .when(metodoPagoService).eliminarMetodoPago(eq(1L), eq(1L), eq(2L));

        mockMvc.perform(delete("/api/usuarios/1/metodos-pago/1")
                        .header("Authorization", "Bearer " + tokenUsuario2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));

        verify(metodoPagoService, times(1)).eliminarMetodoPago(1L, 1L, 2L);
    }

    @Test
    void eliminarMetodoPago_NoExiste() throws Exception {
        doThrow(new MetodoPagoUsuarioNotFoundException(999L))
                .when(metodoPagoService).eliminarMetodoPago(eq(1L), eq(999L), eq(1L));

        mockMvc.perform(delete("/api/usuarios/1/metodos-pago/999")
                        .header("Authorization", "Bearer " + tokenUsuario1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("PAYMENT_NOT_FOUND")));

        verify(metodoPagoService, times(1)).eliminarMetodoPago(1L, 999L, 1L);
    }
}