package com.ondra.users.controllers;

import com.ondra.users.dto.UsuarioPublicoDTO;
import com.ondra.users.services.ArtistaService;
import com.ondra.users.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para perfiles públicos de usuarios y artistas.
 *
 * <p>Proporciona acceso sin autenticación a perfiles mediante slugs únicos.
 * Permite consultar información pública de usuarios y artistas.</p>
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PerfilPublicoController {

    private final UsuarioService usuarioService;
    private final ArtistaService artistaService;

    /**
     * Obtiene el perfil público de un usuario por su slug.
     *
     * @param slug Identificador único del usuario
     * @return Perfil público del usuario
     */
    @GetMapping("/usuarios/{slug}")
    public ResponseEntity<UsuarioPublicoDTO> obtenerPerfilUsuario(@PathVariable String slug) {
        UsuarioPublicoDTO perfil = usuarioService.obtenerPerfilPublicoBySlug(slug);
        return ResponseEntity.ok(perfil);
    }

    /**
     * Obtiene el perfil público de un artista por su slug artístico.
     *
     * @param slugArtistico Identificador único del artista
     * @return Perfil público del artista
     */
    @GetMapping("/artistas/{slugArtistico}")
    public ResponseEntity<UsuarioPublicoDTO> obtenerPerfilArtista(@PathVariable String slugArtistico) {
        UsuarioPublicoDTO perfil = artistaService.obtenerPerfilArtistaPorSlug(slugArtistico);
        return ResponseEntity.ok(perfil);
    }
}