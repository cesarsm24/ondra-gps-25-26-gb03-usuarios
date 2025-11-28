package com.ondra.users.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro para la autenticación entre servicios mediante un token interno.
 *
 * <p>Valida el token enviado en el encabezado {@code X-Service-Token}. Si el valor coincide con
 * el token configurado, se establece una autenticación con privilegios de servicio.</p>
 *
 * <p>Las peticiones con token incorrecto generan una respuesta de error.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceTokenFilter extends OncePerRequestFilter {

    @Value("${microservices.service-token}")
    private String serviceToken;

    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    /**
     * Procesa la validación del token de servicio y establece la autenticación correspondiente.
     *
     * @param request petición HTTP
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros
     * @throws ServletException si ocurre un error en el filtro
     * @throws IOException si ocurre un error de entrada/salida
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestServiceToken = request.getHeader(SERVICE_TOKEN_HEADER);

        if (requestServiceToken == null || requestServiceToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (serviceToken.equals(requestServiceToken)) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            "SERVICE",
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE"))
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            request.setAttribute("isServiceRequest", true);

            log.debug("Autenticación service-to-service establecida");

        } else {
            log.warn("Token de servicio inválido recibido desde {}", request.getRemoteAddr());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_SERVICE_TOKEN", "Token de servicio inválido");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Escribe una respuesta de error en formato JSON.
     *
     * @param response respuesta HTTP
     * @param status código HTTP
     * @param error código de error
     * @param message mensaje descriptivo
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    private void writeErrorResponse(HttpServletResponse response, int status,
                                    String error, String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"}", error, message
        ));
    }
}
