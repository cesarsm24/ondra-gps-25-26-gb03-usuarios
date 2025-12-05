package com.ondra.users.services;
import com.ondra.users.data.ArtistasData;
import com.ondra.users.data.ArtistasData.ArtistaInfo;
import com.ondra.users.data.UsuariosData;
import com.ondra.users.data.UsuariosData.UsuarioInfo;
import com.ondra.users.models.dao.*;
import com.ondra.users.repositories.*;
import com.ondra.users.models.enums.TipoMetodoPago;
import com.ondra.users.models.enums.TipoRedSocial;
import com.ondra.users.models.enums.TipoUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para poblar la base de datos con datos de prueba.
 * Se ejecuta al iniciar la aplicación en el perfil dev.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Profile("dev")
public class DataSeederService implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final ArtistaRepository artistaRepository;
    private final MetodoPagoUsuarioRepository metodoPagoUsuarioRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final MetodoCobroArtistaRepository metodoCobroArtistaRepository;
    private final RedSocialRepository redSocialRepository;
    private final PasswordEncoder passwordEncoder;

    // Instancia única de Random para toda la clase
    private final Random random = new Random();

    private static final String[] PAYMENT_TYPES = {"TARJETA", "TRANSFERENCIA", "PAYPAL", "BIZUM"};
    private static final String[] COBRO_TYPES = {"TRANSFERENCIA", "PAYPAL", "BIZUM"};
    private static final String[] PROVINCIAS = {
            "Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza",
            "Málaga", "Murcia", "Palma", "Las Palmas", "Bilbao"
    };
    @Value("${seed.enabled:false}")
    private boolean seedEnabled;
    @Override
    @Transactional
    public void run(String... args) {

        if (!seedEnabled) {
            log.info("⏭️  Data seeding deshabilitado");
            return;
        }
        log.info("🚀 Iniciando población de base de datos...");

        try {
            log.info("👥 Poblando usuarios normales...");
            poblarUsuarios();
            log.info("✅ Usuarios normales creados: {}", UsuariosData.USUARIOS_PREDEFINIDOS.size());
            log.info("🎵 Poblando artistas...");
            poblarArtistas();
            log.info("✅ Artistas creados: {}", ArtistasData.ARTISTAS_PREDEFINIDOS.size());
            log.info("🔗 Poblando seguimientos...");
            List<Usuario> usuariosNormales = usuarioRepository.findAll().stream()
                    .filter(u -> u.getTipoUsuario() == TipoUsuario.NORMAL)
                    .collect(Collectors.toList());
            int totalSeguimientos = poblarSeguimientos(usuariosNormales);
            log.info("✅ Seguimientos creados: {}", totalSeguimientos);
            log.info("📊 RESUMEN:");
            log.info("   • Usuarios normales: {}", UsuariosData.USUARIOS_PREDEFINIDOS.size());
            log.info("   • Artistas: {}", ArtistasData.ARTISTAS_PREDEFINIDOS.size());
            log.info("   • Seguimientos: {}", totalSeguimientos);
            log.info("   • Contraseña: Usuario2025! / Artista2025!");
        } catch (Exception e) {
            log.error("❌ Error durante la población de datos: {}", e.getMessage(), e);
        }
    }

    /**
     * Crea usuarios normales con sus métodos de pago.
     */
    private void poblarUsuarios() {
        for (int i = 0; i < UsuariosData.USUARIOS_PREDEFINIDOS.size(); i++) {
            UsuarioInfo usuarioInfo = UsuariosData.USUARIOS_PREDEFINIDOS.get(i);
            try {
                log.info("📝 Procesando usuario {}/{}: {}",
                        i + 1, UsuariosData.USUARIOS_PREDEFINIDOS.size(), usuarioInfo.username);
                Usuario usuario = Usuario.builder()
                        .nombreUsuario(usuarioInfo.nombre)
                        .apellidosUsuario(usuarioInfo.apellidos)
                        .emailUsuario(usuarioInfo.username + "@ondrasounds.com")
                        .passwordUsuario(passwordEncoder.encode("Usuario2025!"))
                        .tipoUsuario(TipoUsuario.NORMAL)
                        .onboardingCompletado(false)
                        .fechaRegistro(generarFechaRegistro())
                        .activo(true)
                        .slug(usuarioInfo.username)
                        .emailVerificado(true)
                        .permiteGoogle(random.nextBoolean())
                        .fotoPerfil(usuarioInfo.urlImagenCompartida)
                        .build();
                usuarioRepository.save(usuario);
                int cantidadPagos = random.nextInt(2) + 1;
                generarPagosUsuario(usuario, cantidadPagos);
                log.info("✅ {} completado", usuarioInfo.username);
            } catch (Exception e) {
                log.error("❌ Error al procesar {}: {}", usuarioInfo.username, e.getMessage());
            }
        }
    }

    /**
     * Crea artistas con sus métodos de cobro y redes sociales.
     */
    private void poblarArtistas() {
        for (int i = 0; i < ArtistasData.ARTISTAS_PREDEFINIDOS.size(); i++) {
            ArtistaInfo artistaInfo = ArtistasData.ARTISTAS_PREDEFINIDOS.get(i);
            try {
                log.info("📝 Procesando artista {}/{}: {}",
                        i + 1, ArtistasData.ARTISTAS_PREDEFINIDOS.size(), artistaInfo.nombreArtistico);
                Usuario usuario = crearUsuario(artistaInfo);
                usuario.setFotoPerfil(artistaInfo.urlImagenCompartida);
                usuarioRepository.save(usuario);
                Artista artista = Artista.builder()
                        .usuario(usuario)
                        .nombreArtistico(artistaInfo.nombreArtistico)
                        .biografiaArtistico(artistaInfo.biografia)
                        .fotoPerfilArtistico(artistaInfo.urlImagenCompartida)
                        .fechaInicioArtistico(generarFechaInicioArtistico())
                        .slugArtistico(artistaInfo.username)
                        .esTendencia(artistaInfo.esTendencia)
                        .build();
                artistaRepository.save(artista);
                generarRedesSociales(artista, artistaInfo);
                generarPagosUsuario(usuario, random.nextInt(2) + 1);
                generarPagosArtista(artista, random.nextInt(2) + 1);
                log.info("✅ {} completado", artistaInfo.nombreArtistico);
            } catch (Exception e) {
                log.error("❌ Error al procesar {}: {}", artistaInfo.nombreArtistico, e.getMessage());
            }
        }
    }

    /**
     * Crea el usuario base para un artista.
     */
    private Usuario crearUsuario(ArtistaInfo artistaInfo) {
        String[] partes = artistaInfo.nombreReal.split(" ", 2);
        return Usuario.builder()
                .nombreUsuario(partes[0])
                .apellidosUsuario(partes.length > 1 ? partes[1] : "")
                .emailUsuario(artistaInfo.username + "@ondrasounds.com")
                .passwordUsuario(passwordEncoder.encode("Artista2025!"))
                .tipoUsuario(TipoUsuario.ARTISTA)
                .onboardingCompletado(false)
                .fechaRegistro(generarFechaRegistro())
                .activo(true)
                .slug(artistaInfo.username)
                .emailVerificado(true)
                .permiteGoogle(random.nextBoolean())
                .build();
    }

    /**
     * Genera las redes sociales de un artista.
     */
    private void generarRedesSociales(Artista artista, ArtistaInfo artistaInfo) {
        for (String plataforma : artistaInfo.redesSociales) {
            try {
                RedSocial redSocial = RedSocial.builder()
                        .artista(artista)
                        .tipoRedSocial(TipoRedSocial.valueOf(plataforma))
                        .urlRedSocial(generarUrlRedSocial(plataforma, artistaInfo.username))
                        .build();
                redSocialRepository.save(redSocial);
            } catch (IllegalArgumentException e) {
                log.warn("Red social no válida: {}", plataforma);
            }
        }
    }

    /**
     * Genera métodos de pago aleatorios para un usuario.
     */
    private void generarPagosUsuario(Usuario usuario, int cantidad) {
        Set<TipoMetodoPago> metodosUsados = new HashSet<>();
        for (int i = 0; i < cantidad; i++) {
            TipoMetodoPago tipoMetodoPago;
            do {
                tipoMetodoPago = TipoMetodoPago.valueOf(PAYMENT_TYPES[random.nextInt(PAYMENT_TYPES.length)]);
            } while (metodosUsados.contains(tipoMetodoPago) && metodosUsados.size() < PAYMENT_TYPES.length);
            metodosUsados.add(tipoMetodoPago);
            MetodoPagoUsuario pago = MetodoPagoUsuario.builder()
                    .usuario(usuario)
                    .tipoPago(tipoMetodoPago)
                    .propietario(usuario.getNombreUsuario() + " " + usuario.getApellidosUsuario())
                    .direccion("Calle " + (random.nextInt(100) + 1) + ", " + (random.nextInt(10) + 1) + "º")
                    .pais("España")
                    .provincia(PROVINCIAS[random.nextInt(PROVINCIAS.length)])
                    .codigoPostal(String.format("%05d", random.nextInt(52999) + 1000))
                    .build();
            switch (tipoMetodoPago) {
                case TARJETA:
                    pago.setNumeroTarjeta(generarNumeroTarjeta());
                    pago.setFechaCaducidad(generarFechaCaducidad());
                    pago.setCvv(String.format("%03d", random.nextInt(1000)));
                    break;
                case PAYPAL:
                    pago.setEmailPaypal(usuario.getEmailUsuario());
                    break;
                case BIZUM:
                    pago.setTelefonoBizum(generarTelefono());
                    break;
                case TRANSFERENCIA:
                    pago.setIban(generarIBAN());
                    break;
            }
            metodoPagoUsuarioRepository.save(pago);
        }
    }

    /**
     * Genera métodos de cobro aleatorios para un artista.
     */
    private void generarPagosArtista(Artista artista, int cantidad) {
        Set<TipoMetodoPago> metodosUsados = new HashSet<>();
        for (int i = 0; i < cantidad; i++) {
            TipoMetodoPago metodoCobro;
            do {
                metodoCobro = TipoMetodoPago.valueOf(COBRO_TYPES[random.nextInt(COBRO_TYPES.length)]);
            } while (metodosUsados.contains(metodoCobro) && metodosUsados.size() < COBRO_TYPES.length);
            metodosUsados.add(metodoCobro);
            MetodoCobroArtista cobro = MetodoCobroArtista.builder()
                    .artista(artista)
                    .tipoCobro(metodoCobro)
                    .propietario(artista.getUsuario().getNombreUsuario() + " " +
                            artista.getUsuario().getApellidosUsuario())
                    .direccion("Calle " + (random.nextInt(100) + 1) + ", " + (random.nextInt(10) + 1) + "º")
                    .pais("España")
                    .provincia(PROVINCIAS[random.nextInt(PROVINCIAS.length)])
                    .codigoPostal(String.format("%05d", random.nextInt(52999) + 1000))
                    .build();
            switch (metodoCobro) {
                case PAYPAL:
                    cobro.setEmailPaypal(artista.getUsuario().getEmailUsuario());
                    break;
                case BIZUM:
                    cobro.setTelefonoBizum(generarTelefono());
                    break;
                case TRANSFERENCIA:
                    cobro.setIban(generarIBAN());
                    break;
            }
            metodoCobroArtistaRepository.save(cobro);
        }
    }

    /**
     * Crea seguimientos entre usuarios normales y artistas/usuarios.
     */
    private int poblarSeguimientos(List<Usuario> usuariosNormales) {
        int totalSeguimientos = 0;
        List<Usuario> artistas = usuarioRepository.findAll().stream()
                .filter(u -> u.getTipoUsuario() == TipoUsuario.ARTISTA)
                .collect(Collectors.toList());
        for (Usuario seguidor : usuariosNormales) {
            int cantidadArtistas = (int) (artistas.size() * (0.6 + random.nextDouble() * 0.2));
            List<Usuario> artistasSeleccionados = seleccionarAleatorios(artistas, cantidadArtistas);
            for (Usuario artista : artistasSeleccionados) {
                if (crearSeguimiento(seguidor, artista)) {
                    totalSeguimientos++;
                }
            }
            int cantidadUsuarios = (int) (usuariosNormales.size() * (0.3 + random.nextDouble() * 0.2));
            List<Usuario> usuariosSeleccionados = seleccionarAleatorios(usuariosNormales, cantidadUsuarios);
            for (Usuario seguido : usuariosSeleccionados) {
                if (!seguido.getIdUsuario().equals(seguidor.getIdUsuario())) {
                    if (crearSeguimiento(seguidor, seguido)) {
                        totalSeguimientos++;
                    }
                }
            }
        }
        return totalSeguimientos;
    }

    /**
     * Crea un seguimiento si no existe previamente.
     */
    private boolean crearSeguimiento(Usuario seguidor, Usuario seguido) {
        try {
            boolean existe = seguimientoRepository.existsBySeguidor_IdUsuarioAndSeguido_IdUsuario(
                    seguidor.getIdUsuario(), seguido.getIdUsuario());
            if (existe) return false;
            Seguimiento seguimiento = Seguimiento.builder()
                    .seguidor(seguidor)
                    .seguido(seguido)
                    .fechaSeguimiento(generarFechaSeguimiento())
                    .build();
            seguimientoRepository.save(seguimiento);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Selecciona elementos aleatorios de una lista.
     */
    private <T> List<T> seleccionarAleatorios(List<T> lista, int cantidad) {
        if (cantidad >= lista.size()) return new ArrayList<>(lista);
        List<T> copia = new ArrayList<>(lista);
        Collections.shuffle(copia, random);
        return copia.subList(0, Math.min(cantidad, copia.size()));
    }

    private String generarUrlRedSocial(String plataforma, String username) {
        switch (plataforma.toUpperCase()) {
            case "INSTAGRAM": return "https://instagram.com/" + username;
            case "X": return "https://x.com/" + username;
            case "FACEBOOK": return "https://facebook.com/" + username;
            case "TIKTOK": return "https://tiktok.com/@" + username;
            case "YOUTUBE": return "https://youtube.com/@" + username;
            case "SPOTIFY": return "https://open.spotify.com/artist/" + username;
            default: return "https://" + plataforma.toLowerCase() + ".com/" + username;
        }
    }

    private String generarIBAN() {
        StringBuilder iban = new StringBuilder("ES");
        for (int i = 0; i < 22; i++) iban.append(random.nextInt(10));
        return iban.toString();
    }

    private LocalDateTime generarFechaRegistro() {
        return LocalDateTime.now().minusDays(1L + random.nextInt(730));
    }

    private LocalDateTime generarFechaInicioArtistico() {
        return LocalDateTime.now().minusDays(1L + random.nextInt(1825));
    }

    private LocalDateTime generarFechaSeguimiento() {
        return LocalDateTime.now().minusDays(1L + random.nextInt(365));
    }

    private String generarNumeroTarjeta() {
        StringBuilder numero = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            numero.append(random.nextInt(10));
            if ((i + 1) % 4 == 0 && i < 15) numero.append(" ");
        }
        return numero.toString();
    }

    private String generarFechaCaducidad() {
        int mes = random.nextInt(12) + 1;
        int año = LocalDateTime.now().getYear() + random.nextInt(5) + 1;
        return String.format("%02d/%02d", mes, año % 100);
    }

    private String generarTelefono() {
        return String.format("+34 %d%d%d %d%d%d %d%d%d",
                6 + random.nextInt(2), random.nextInt(10), random.nextInt(10),
                random.nextInt(10), random.nextInt(10), random.nextInt(10),
                random.nextInt(10), random.nextInt(10), random.nextInt(10));
    }
}