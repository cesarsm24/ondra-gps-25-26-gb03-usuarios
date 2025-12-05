package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.security.JwtAuthenticationFilter;
import com.ondra.users.security.SecurityConfig;
import com.ondra.users.security.ServiceTokenFilter;
import com.ondra.users.controllers.TestJwtHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para {@link ConfigController}.
 *
 * <p>Se centra en la obtención de la configuración pública de la aplicación.
 * Configura filtros de seguridad y propiedades de prueba.</p>
 */
@WebMvcTest(ConfigController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {
        "google.oauth.client-id=TEST_GOOGLE_CLIENT",
        "spring.application.name=TestApplication"
})
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Prueba la obtención de la configuración pública de la aplicación.
     * Verifica que los valores de googleClientId y appName se devuelvan correctamente.
     *
     * @throws Exception si ocurre un error durante la ejecución de la solicitud
     */
    @Test
    @DisplayName("Obtener configuración pública - exitoso")
    void obtenerConfigPublica_Success() throws Exception {
        mockMvc.perform(get("/api/config/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.googleClientId").value("TEST_GOOGLE_CLIENT"))
                .andExpect(jsonPath("$.appName").value("TestApplication"));
    }
}
