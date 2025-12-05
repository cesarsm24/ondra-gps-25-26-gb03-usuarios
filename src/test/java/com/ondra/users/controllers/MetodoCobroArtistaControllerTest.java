package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.dto.MetodoCobroArtistaCrearDTO;
import com.ondra.users.dto.MetodoCobroArtistaDTO;
import com.ondra.users.dto.MetodoCobroArtistaEditarDTO;
import com.ondra.users.exceptions.ArtistaNotFoundException;
import com.ondra.users.exceptions.ForbiddenAccessException;
import com.ondra.users.exceptions.InvalidPaymentMethodException;
import com.ondra.users.exceptions.MetodoPagoUsuarioNotFoundException;
import com.ondra.users.services.MetodoCobroArtistaService;
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
 * Tests unitarios para {@link MetodoCobroArtistaController}.
 *
 * <p>Se verifican los endpoints relacionados con la gestión de métodos de cobro de artistas:
 * listar, crear, editar y eliminar métodos de cobro.</p>
 *
 * <p>Se incluyen casos de éxito, errores, validaciones y restricciones de permisos.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class MetodoCobroArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetodoCobroArtistaService metodoCobroService;

    @Autowired
    private TestJwtHelper jwtHelper;

    private String tokenArtista1;
    private String tokenArtista2;

    @BeforeEach
    void setUp() {
        // Usuario 1 es artista con ID 1
        tokenArtista1 = jwtHelper.generarTokenPruebaArtista(1L, 1L, "artista1@example.com");
        // Usuario 2 es artista con ID 2
        tokenArtista2 = jwtHelper.generarTokenPruebaArtista(2L, 2L, "artista2@example.com");
    }

    // ========== TESTS DE LISTAR MÉTODOS DE COBRO ==========

    @Test
    void listarMetodosCobro_Success() throws Exception {
        MetodoCobroArtistaDTO metodo1 = MetodoCobroArtistaDTO.builder()
                .idMetodoCobro(1L)
                .tipo("paypal")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .emailPaypal("artista@paypal.com")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        MetodoCobroArtistaDTO metodo2 = MetodoCobroArtistaDTO.builder()
                .idMetodoCobro(2L)
                .tipo("transferencia")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .iban("ES9121000418450200051332")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoCobroService.listarMetodosCobro(eq(1L), eq(1L)))
                .thenReturn(Arrays.asList(metodo1, metodo2));

        mockMvc.perform(get("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idMetodoCobro", is(1)))
                .andExpect(jsonPath("$[0].tipo", is("paypal")))
                .andExpect(jsonPath("$[1].idMetodoCobro", is(2)))
                .andExpect(jsonPath("$[1].tipo", is("transferencia")));

        verify(metodoCobroService, times(1)).listarMetodosCobro(1L, 1L);
    }

    @Test
    void listarMetodosCobro_ListaVacia() throws Exception {
        when(metodoCobroService.listarMetodosCobro(eq(1L), eq(1L)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(metodoCobroService, times(1)).listarMetodosCobro(1L, 1L);
    }

    @Test
    void listarMetodosCobro_SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/artistas/1/metodos-cobro")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(metodoCobroService, never()).listarMetodosCobro(any(), any());
    }

    @Test
    void listarMetodosCobro_OtroArtista_Forbidden() throws Exception {
        when(metodoCobroService.listarMetodosCobro(eq(1L), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para ver los métodos de cobro de este artista"));

        mockMvc.perform(get("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));

        verify(metodoCobroService, times(1)).listarMetodosCobro(1L, 2L);
    }

    @Test
    void listarMetodosCobro_ArtistaNoExiste() throws Exception {
        when(metodoCobroService.listarMetodosCobro(eq(999L), eq(999L)))
                .thenThrow(new ArtistaNotFoundException(999L));

        String tokenArtista999 = jwtHelper.generarTokenPruebaArtista(999L, 999L, "noexiste@example.com");

        mockMvc.perform(get("/api/artistas/999/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("ARTIST_NOT_FOUND")));

        verify(metodoCobroService, times(1)).listarMetodosCobro(999L, 999L);
    }

    // ========== TESTS DE CREAR MÉTODOS DE COBRO ==========

    @Test
    void crearMetodoCobroPaypal_Success() throws Exception {
        MetodoCobroArtistaCrearDTO crearDTO = MetodoCobroArtistaCrearDTO.builder()
                .metodoPago("PAYPAL")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .emailPaypal("artista@paypal.com")
                .build();

        MetodoCobroArtistaDTO metodoCreado = MetodoCobroArtistaDTO.builder()
                .idMetodoCobro(1L)
                .tipo("paypal")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .emailPaypal("artista@paypal.com")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoCobroService.crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(1L)))
                .thenReturn(metodoCreado);

        mockMvc.perform(post("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMetodoCobro", is(1)))
                .andExpect(jsonPath("$.tipo", is("paypal")))
                .andExpect(jsonPath("$.emailPaypal", is("artista@paypal.com")));

        verify(metodoCobroService, times(1)).crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoCobroBizum_Success() throws Exception {
        MetodoCobroArtistaCrearDTO crearDTO = MetodoCobroArtistaCrearDTO.builder()
                .metodoPago("BIZUM")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .telefonoBizum("+34612345678")
                .build();

        MetodoCobroArtistaDTO metodoCreado = MetodoCobroArtistaDTO.builder()
                .idMetodoCobro(2L)
                .tipo("bizum")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .telefonoBizum("+34612345678")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoCobroService.crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(1L)))
                .thenReturn(metodoCreado);

        mockMvc.perform(post("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMetodoCobro", is(2)))
                .andExpect(jsonPath("$.tipo", is("bizum")))
                .andExpect(jsonPath("$.telefonoBizum", is("+34612345678")));

        verify(metodoCobroService, times(1)).crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoCobroTransferencia_Success() throws Exception {
        MetodoCobroArtistaCrearDTO crearDTO = MetodoCobroArtistaCrearDTO.builder()
                .metodoPago("TRANSFERENCIA")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .iban("ES9121000418450200051332")
                .build();

        MetodoCobroArtistaDTO metodoCreado = MetodoCobroArtistaDTO.builder()
                .idMetodoCobro(3L)
                .tipo("transferencia")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .iban("ES9121000418450200051332")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("01/01/2024 10:00")
                .build();

        when(metodoCobroService.crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(1L)))
                .thenReturn(metodoCreado);

        mockMvc.perform(post("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMetodoCobro", is(3)))
                .andExpect(jsonPath("$.tipo", is("transferencia")))
                .andExpect(jsonPath("$.iban", is("ES9121000418450200051332")));

        verify(metodoCobroService, times(1)).crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoCobro_TipoTarjetaNoPermitido() throws Exception {
        MetodoCobroArtistaCrearDTO crearDTO = MetodoCobroArtistaCrearDTO.builder()
                .metodoPago("TARJETA")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .build();

        mockMvc.perform(post("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Método de cobro no válido")));

        verify(metodoCobroService, never()).crearMetodoCobro(any(), any(), any());
    }

    @Test
    void crearMetodoCobro_TipoInvalido() throws Exception {
        MetodoCobroArtistaCrearDTO crearDTO = MetodoCobroArtistaCrearDTO.builder()
                .metodoPago("CRIPTOMONEDA")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .build();

        mockMvc.perform(post("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));

        verify(metodoCobroService, never()).crearMetodoCobro(any(), any(), any());
    }

    @Test
    void crearMetodoCobroPaypal_CamposFaltantes() throws Exception {
        MetodoCobroArtistaCrearDTO crearDTO = MetodoCobroArtistaCrearDTO.builder()
                .metodoPago("PAYPAL")
                .propietario("Artista Uno")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                // Falta emailPaypal
                .build();

        when(metodoCobroService.crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(1L)))
                .thenThrow(new InvalidPaymentMethodException("PayPal requiere: emailPaypal"));

        mockMvc.perform(post("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYMENT_METHOD")));

        verify(metodoCobroService, times(1)).crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(1L));
    }

    @Test
    void crearMetodoCobro_OtroArtista_Forbidden() throws Exception {
        MetodoCobroArtistaCrearDTO crearDTO = MetodoCobroArtistaCrearDTO.builder()
                .metodoPago("PAYPAL")
                .propietario("Intruso")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .emailPaypal("intruso@paypal.com")
                .build();

        when(metodoCobroService.crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para añadir métodos de cobro a este artista"));

        mockMvc.perform(post("/api/artistas/1/metodos-cobro")
                        .header("Authorization", "Bearer " + tokenArtista2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));

        verify(metodoCobroService, times(1)).crearMetodoCobro(eq(1L), any(MetodoCobroArtistaCrearDTO.class), eq(2L));
    }

    // ========== TESTS DE EDITAR MÉTODOS DE COBRO ==========

    @Test
    void editarMetodoCobro_Success() throws Exception {
        MetodoCobroArtistaEditarDTO editarDTO = MetodoCobroArtistaEditarDTO.builder()
                .propietario("Artista Uno Actualizado")
                .emailPaypal("nuevo@paypal.com")
                .build();

        MetodoCobroArtistaDTO metodoActualizado = MetodoCobroArtistaDTO.builder()
                .idMetodoCobro(1L)
                .tipo("paypal")
                .propietario("Artista Uno Actualizado")
                .direccion("Calle Artista 123")
                .pais("España")
                .provincia("Barcelona")
                .codigoPostal("08001")
                .emailPaypal("nuevo@paypal.com")
                .fechaCreacion("01/01/2024 10:00")
                .fechaActualizacion("02/01/2024 15:00")
                .build();

        when(metodoCobroService.editarMetodoCobro(eq(1L), eq(1L), any(MetodoCobroArtistaEditarDTO.class), eq(1L)))
                .thenReturn(metodoActualizado);

        mockMvc.perform(put("/api/artistas/1/metodos-cobro/1")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propietario", is("Artista Uno Actualizado")))
                .andExpect(jsonPath("$.emailPaypal", is("nuevo@paypal.com")));

        verify(metodoCobroService, times(1)).editarMetodoCobro(eq(1L), eq(1L), any(MetodoCobroArtistaEditarDTO.class), eq(1L));
    }

    @Test
    void editarMetodoCobro_OtroArtista_Forbidden() throws Exception {
        MetodoCobroArtistaEditarDTO editarDTO = MetodoCobroArtistaEditarDTO.builder()
                .propietario("Intruso")
                .build();

        when(metodoCobroService.editarMetodoCobro(eq(1L), eq(1L), any(MetodoCobroArtistaEditarDTO.class), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para modificar métodos de cobro de este artista"));

        mockMvc.perform(put("/api/artistas/1/metodos-cobro/1")
                        .header("Authorization", "Bearer " + tokenArtista2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));

        verify(metodoCobroService, times(1)).editarMetodoCobro(eq(1L), eq(1L), any(MetodoCobroArtistaEditarDTO.class), eq(2L));
    }

    @Test
    void editarMetodoCobro_NoExiste() throws Exception {
        MetodoCobroArtistaEditarDTO editarDTO = MetodoCobroArtistaEditarDTO.builder()
                .propietario("Artista Uno")
                .build();

        when(metodoCobroService.editarMetodoCobro(eq(1L), eq(999L), any(MetodoCobroArtistaEditarDTO.class), eq(1L)))
                .thenThrow(new MetodoPagoUsuarioNotFoundException(999L));

        mockMvc.perform(put("/api/artistas/1/metodos-cobro/999")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("PAYMENT_NOT_FOUND")));

        verify(metodoCobroService, times(1)).editarMetodoCobro(eq(1L), eq(999L), any(MetodoCobroArtistaEditarDTO.class), eq(1L));
    }

    // ========== TESTS DE ELIMINAR MÉTODOS DE COBRO ==========

    @Test
    void eliminarMetodoCobro_Success() throws Exception {
        doNothing().when(metodoCobroService).eliminarMetodoCobro(eq(1L), eq(1L), eq(1L));

        mockMvc.perform(delete("/api/artistas/1/metodos-cobro/1")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Método de cobro eliminado correctamente"));

        verify(metodoCobroService, times(1)).eliminarMetodoCobro(1L, 1L, 1L);
    }

    @Test
    void eliminarMetodoCobro_SinAutenticacion() throws Exception {
        mockMvc.perform(delete("/api/artistas/1/metodos-cobro/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(metodoCobroService, never()).eliminarMetodoCobro(any(), any(), any());
    }

    @Test
    void eliminarMetodoCobro_OtroArtista_Forbidden() throws Exception {
        doThrow(new ForbiddenAccessException("No tienes permiso para eliminar este método de cobro"))
                .when(metodoCobroService).eliminarMetodoCobro(eq(1L), eq(1L), eq(2L));

        mockMvc.perform(delete("/api/artistas/1/metodos-cobro/1")
                        .header("Authorization", "Bearer " + tokenArtista2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));

        verify(metodoCobroService, times(1)).eliminarMetodoCobro(1L, 1L, 2L);
    }

    @Test
    void eliminarMetodoCobro_NoExiste() throws Exception {
        doThrow(new MetodoPagoUsuarioNotFoundException(999L))
                .when(metodoCobroService).eliminarMetodoCobro(eq(1L), eq(999L), eq(1L));

        mockMvc.perform(delete("/api/artistas/1/metodos-cobro/999")
                        .header("Authorization", "Bearer " + tokenArtista1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("PAYMENT_NOT_FOUND")));

        verify(metodoCobroService, times(1)).eliminarMetodoCobro(1L, 999L, 1L);
    }
}