package com.ondra.users.services;

import com.ondra.users.data.ArtistasData;
import com.ondra.users.data.ArtistasData.ArtistaInfo;
import com.ondra.users.data.UsuariosData;
import com.ondra.users.data.UsuariosData.UsuarioInfo;
import com.ondra.users.models.dao.*;
import com.ondra.users.repositories.*;
import com.ondra.users.models.enums.EstadoPago;
import com.ondra.users.models.enums.TipoMetodoPago;
import com.ondra.users.models.enums.TipoRedSocial;
import com.ondra.users.models.enums.TipoUsuario;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de inicialización de datos para entornos de desarrollo.
 *
 * <p>Puebla la base de datos con usuarios, artistas y relaciones predefinidas
 * al iniciar la aplicación. Solo se ejecuta en el perfil dev cuando la propiedad
 * seed.enabled está activa.</p>
 *
 * <p>Genera automáticamente:</p>
 * <ul>
 *   <li>Usuarios normales con credenciales y métodos de pago</li>
 *   <li>Perfiles de artista con biografía y redes sociales</li>
 *   <li>Métodos de cobro para artistas</li>
 *   <li>Relaciones de seguimiento entre usuarios</li>
 *   <li>Imágenes de perfil subidas a Cloudinary</li>
 * </ul>
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
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final SlugGeneratorService slugGeneratorService;

    private static final String IMAGES_PATH = "src/main/resources/images/artistas";
    private static final String IMAGES_PATH_USUARIOS = "src/main/resources/images/usuarios";
    private static final String[] PAYMENT_TYPES = {"TARJETA", "TRANSFERENCIA", "PAYPAL", "BIZUM"};
    private static final String[] COBRO_TYPES = {"TRANSFERENCIA", "PAYPAL", "BIZUM"};
    private static final EstadoPago[] ESTADOS_PAGO = {EstadoPago.PENDIENTE, EstadoPago.COMPLETADO, EstadoPago.FALLIDO};
    private static final String[] PROVINCIAS = {
            "Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza",
            "Málaga", "Murcia", "Palma", "Las Palmas", "Bilbao"
    };

    @Value("${seed.enabled:false}")
    private boolean seedEnabled;

    /**
     * Ejecuta el proceso de población de datos al iniciar la aplicación.
     * Verifica que el seeding esté habilitado antes de proceder.
     *
     * @param args argumentos de línea de comandos
     */
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

            log.info("🧹 Limpiando carpeta de imágenes de artistas en Cloudinary...");
            int imagenesEliminadas = cloudinaryService.limpiarCarpeta("artistProfile");
            log.info("✅ Carpeta limpiada: {} imágenes eliminadas", imagenesEliminadas);

            log.info("🎵 Poblando artistas...");
            List<String> imageUrls = poblarArtistas();
            log.info("✅ Población completada exitosamente");
            log.info("📸 Total de imágenes subidas: {}", imageUrls.size());
            log.info("🎵 Total de artistas creados: {}", ArtistasData.ARTISTAS_PREDEFINIDOS.size());

            log.info("🔗 Poblando seguimientos entre usuarios y artistas...");
            List<Usuario> usuariosNormales = usuarioRepository.findAll().stream()
                    .filter(u -> u.getTipoUsuario() == TipoUsuario.NORMAL)
                    .collect(Collectors.toList());
            int totalSeguimientos = poblarSeguimientos(usuariosNormales);
            log.info("✅ Seguimientos creados correctamente: {}", totalSeguimientos);

            log.info("📊 RESUMEN:");
            log.info("   • Usuarios normales: {}", UsuariosData.USUARIOS_PREDEFINIDOS.size());
            log.info("   • Artistas: {}", ArtistasData.ARTISTAS_PREDEFINIDOS.size());
            log.info("   • Total usuarios: {}",
                    UsuariosData.USUARIOS_PREDEFINIDOS.size() + ArtistasData.ARTISTAS_PREDEFINIDOS.size());
            log.info("   • Seguimientos creados: {}", totalSeguimientos);
            log.info("   • Contraseña para todos: Usuario2025! (usuarios) / Artista2025! (artistas)");

        } catch (Exception e) {
            log.error("❌ Error durante la población de datos: {}", e.getMessage(), e);
        }
    }

    /**
     * Crea usuarios normales con sus métodos de pago e imágenes de perfil.
     * Limpia la carpeta de imágenes en Cloudinary antes de subir nuevas imágenes.
     */
    private void poblarUsuarios() {
        log.info("🧹 Limpiando carpeta de imágenes de usuarios en Cloudinary...");
        try {
            int imagenesEliminadas = cloudinaryService.limpiarCarpeta("userProfile");
            log.info("✅ Carpeta limpiada: {} imágenes eliminadas", imagenesEliminadas);
        } catch (Exception e) {
            log.warn("⚠️  No se pudo limpiar la carpeta userProfile: {}", e.getMessage());
        }

        List<File> imageFiles = cargarImagenesDesdeDirectorio(IMAGES_PATH_USUARIOS);
        List<String> imageUrls = new ArrayList<>();

        for (int i = 0; i < UsuariosData.USUARIOS_PREDEFINIDOS.size(); i++) {
            UsuarioInfo usuarioInfo = UsuariosData.USUARIOS_PREDEFINIDOS.get(i);

            try {
                log.info("📝 Procesando usuario {} de {}: {}",
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
                        .slug(slugGeneratorService.generarSlugUsuario(
                                usuarioInfo.nombre,
                                usuarioInfo.apellidos
                        ))
                        .emailVerificado(true)
                        .permiteGoogle(new Random().nextBoolean())
                        .build();

                if (!imageFiles.isEmpty()) {
                    try {
                        File imageFile = encontrarImagenUsuario(imageFiles, usuarioInfo.username, i);
                        String imageUrl = subirImagenDesdeArchivo(imageFile, "userProfile");
                        usuario.setFotoPerfil(imageUrl);
                        imageUrls.add(imageUrl);
                        log.debug("  ✓ Imagen subida: {}", imageFile.getName());
                    } catch (IOException e) {
                        log.warn("  ⚠ No se pudo subir imagen para {}: {}",
                                usuarioInfo.username, e.getMessage());
                    }
                }

                usuarioRepository.save(usuario);
                log.debug("  ✓ Usuario creado: {}", usuario.getEmailUsuario());

                int cantidadPagos = new Random().nextInt(2) + 1;
                generarPagosUsuario(usuario, cantidadPagos);
                log.debug("  ✓ {} métodos de pago añadidos", cantidadPagos);

                log.info("✅ {} completado", usuarioInfo.username);

            } catch (Exception e) {
                log.error("❌ Error al procesar {}: {}", usuarioInfo.username, e.getMessage());
            }
        }

        log.info("📸 Total de imágenes de usuarios subidas: {}", imageUrls.size());
    }

    /**
     * Escanea un directorio y retorna todos los archivos de imagen válidos.
     * Soporta formatos: JPG, JPEG, PNG, WEBP.
     *
     * @param directorio ruta del directorio a escanear
     * @return lista de archivos de imagen encontrados
     */
    private List<File> cargarImagenesDesdeDirectorio(String directorio) {
        try {
            Path path = Paths.get(directorio);

            if (!Files.exists(path)) {
                log.warn("⚠️  Directorio de imágenes no encontrado: {}", directorio);
                return new ArrayList<>();
            }

            List<File> files = new ArrayList<>();
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String fileName = p.toString().toLowerCase();
                        return fileName.endsWith(".jpg") ||
                                fileName.endsWith(".jpeg") ||
                                fileName.endsWith(".png") ||
                                fileName.endsWith(".webp");
                    })
                    .forEach(p -> files.add(p.toFile()));

            log.info("📁 {} imágenes encontradas en {}", files.size(), directorio);
            return files;

        } catch (IOException e) {
            log.error("❌ Error al cargar imágenes de {}: {}", directorio, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Crea perfiles de artista con sus usuarios asociados, redes sociales y métodos de cobro.
     *
     * @return lista de URLs de imágenes subidas a Cloudinary
     */
    private List<String> poblarArtistas() {
        List<String> imageUrls = new ArrayList<>();
        List<File> imageFiles = cargarImagenesDesdeDirectorio(IMAGES_PATH);

        for (int i = 0; i < ArtistasData.ARTISTAS_PREDEFINIDOS.size(); i++) {
            ArtistaInfo artistaInfo = ArtistasData.ARTISTAS_PREDEFINIDOS.get(i);

            try {
                log.info("📝 Procesando artista {} de {}: {}",
                        i + 1, ArtistasData.ARTISTAS_PREDEFINIDOS.size(), artistaInfo.nombreArtistico);

                Usuario usuario = crearUsuario(artistaInfo);
                usuarioRepository.save(usuario);
                log.debug("  ✓ Usuario creado: {}", usuario.getEmailUsuario());

                Artista artista = crearArtista(usuario, artistaInfo, imageFiles, imageUrls, i);
                artistaRepository.save(artista);
                log.debug("  ✓ Artista creado: {}", artista.getNombreArtistico());

                generarRedesSociales(artista, artistaInfo);
                log.debug("  ✓ {} redes sociales añadidas", artistaInfo.redesSociales.size());

                int cantidadPagosUsuario = new Random().nextInt(2) + 1;
                generarPagosUsuario(usuario, cantidadPagosUsuario);
                log.debug("  ✓ {} métodos de pago de usuario añadidos", cantidadPagosUsuario);

                int cantidadPagosArtista = new Random().nextInt(2) + 1;
                generarPagosArtista(artista, cantidadPagosArtista);
                log.debug("  ✓ {} métodos de cobro añadidos", cantidadPagosArtista);

                log.info("✅ {} completado", artistaInfo.nombreArtistico);

            } catch (Exception e) {
                log.error("❌ Error al procesar {}: {}", artistaInfo.nombreArtistico, e.getMessage());
            }
        }

        return imageUrls;
    }

    /**
     * Construye una entidad Usuario con tipo ARTISTA.
     *
     * @param artistaInfo datos del artista predefinido
     * @return usuario configurado
     */
    private Usuario crearUsuario(ArtistaInfo artistaInfo) {
        String email = generarEmail(artistaInfo.username);
        String[] partes = artistaInfo.nombreReal.split(" ", 2);

        return Usuario.builder()
                .nombreUsuario(partes[0])
                .apellidosUsuario(partes.length > 1 ? partes[1] : "")
                .emailUsuario(email)
                .passwordUsuario(passwordEncoder.encode("Artista2025!"))
                .tipoUsuario(TipoUsuario.ARTISTA)
                .onboardingCompletado(false)
                .fechaRegistro(generarFechaRegistro())
                .activo(true)
                .slug(slugGeneratorService.generarSlugUsuario(
                        partes[0],
                        partes.length > 1 ? partes[1] : ""
                ))
                .emailVerificado(true)
                .permiteGoogle(new Random().nextBoolean())
                .build();
    }

    /**
     * Construye un perfil de artista y sube su imagen a Cloudinary.
     * Asigna la imagen tanto al perfil de artista como al usuario asociado.
     *
     * @param usuario usuario asociado al artista
     * @param artistaInfo datos del artista predefinido
     * @param imageFiles archivos de imagen disponibles
     * @param imageUrls lista acumulativa de URLs subidas
     * @param index índice para selección de imagen por defecto
     * @return perfil de artista configurado
     */
    private Artista crearArtista(Usuario usuario, ArtistaInfo artistaInfo,
                                 List<File> imageFiles, List<String> imageUrls, int index) {

        Artista artista = Artista.builder()
                .usuario(usuario)
                .nombreArtistico(artistaInfo.nombreArtistico)
                .biografiaArtistico(artistaInfo.biografia)
                .fotoPerfilArtistico("https://res.cloudinary.com/dh6w4hrx7/image/upload/v1759703978/default_iqtgmk.jpg")
                .fechaInicioArtistico(generarFechaInicioArtistico())
                .slugArtistico(slugGeneratorService.generarSlugArtista(
                        artistaInfo.nombreArtistico
                ))
                .esTendencia(artistaInfo.esTendencia)
                .build();

        if (!imageFiles.isEmpty()) {
            try {
                File imageFile = encontrarImagenArtista(imageFiles, artistaInfo.username, index);
                String imageUrl = subirImagenDesdeArchivo(imageFile, "artistProfile");

                artista.setFotoPerfilArtistico(imageUrl);
                usuario.setFotoPerfil(imageUrl);

                imageUrls.add(imageUrl);
                log.debug("  ✓ Imagen subida y asignada: {}", imageFile.getName());
            } catch (IOException e) {
                log.warn("  ⚠ No se pudo subir imagen para {}: {}",
                        artistaInfo.nombreArtistico, e.getMessage());
            }
        }

        return artista;
    }

    /**
     * Crea y persiste redes sociales para un artista.
     *
     * @param artista entidad del artista
     * @param artistaInfo datos con plataformas sociales
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
                log.warn("  ⚠ Red social no válida: {}", plataforma);
            }
        }
    }

    /**
     * Genera métodos de pago aleatorios para un usuario.
     * Evita duplicados del mismo tipo de método.
     *
     * @param usuario usuario al que se asignan los métodos
     * @param cantidad número de métodos a generar
     */
    private void generarPagosUsuario(Usuario usuario, int cantidad) {
        Random random = new Random();
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
     * No incluye TARJETA como opción válida para artistas.
     *
     * @param artista artista al que se asignan los métodos
     * @param cantidad número de métodos a generar
     */
    private void generarPagosArtista(Artista artista, int cantidad) {
        Random random = new Random();
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
     * Busca una imagen específica por username o retorna imagen por índice.
     *
     * @param imageFiles archivos disponibles
     * @param username nombre a buscar en archivos
     * @param defaultIndex índice de fallback
     * @return archivo de imagen seleccionado
     */
    private File encontrarImagenArtista(List<File> imageFiles, String username, int defaultIndex) {
        for (File file : imageFiles) {
            String fileName = file.getName().toLowerCase();
            if (fileName.contains(username.toLowerCase())) {
                return file;
            }
        }
        return imageFiles.get(defaultIndex % imageFiles.size());
    }

    /**
     * Busca una imagen específica por username o retorna imagen por índice.
     *
     * @param imageFiles archivos disponibles
     * @param username nombre a buscar en archivos
     * @param defaultIndex índice de fallback
     * @return archivo de imagen seleccionado
     */
    private File encontrarImagenUsuario(List<File> imageFiles, String username, int defaultIndex) {
        for (File file : imageFiles) {
            String fileName = file.getName().toLowerCase();
            if (fileName.contains(username.toLowerCase())) {
                return file;
            }
        }
        return imageFiles.get(defaultIndex % imageFiles.size());
    }

    /**
     * Sube una imagen local a Cloudinary.
     * Convierte el archivo a MultipartFile antes de enviarlo.
     *
     * @param file archivo de imagen local
     * @param folder carpeta destino en Cloudinary
     * @return URL pública de la imagen subida
     * @throws IOException si falla la lectura o subida
     */
    private String subirImagenDesdeArchivo(File file, String folder) throws IOException {
        byte[] content = Files.readAllBytes(file.toPath());
        String contentType = Files.probeContentType(file.toPath());

        if (contentType == null) {
            contentType = "image/jpeg";
        }

        final String finalContentType = contentType;

        MultipartFile multipartFile = new MultipartFile() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return file.getName();
            }

            @Override
            public String getContentType() {
                return finalContentType;
            }

            @Override
            public boolean isEmpty() {
                return content.length == 0;
            }

            @Override
            public long getSize() {
                return content.length;
            }

            @Override
            public byte[] getBytes() {
                return content;
            }

            @Override
            public java.io.InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.copy(file.toPath(), dest.toPath());
            }
        };

        return cloudinaryService.subirImagen(multipartFile, folder);
    }

    /**
     * Genera email corporativo basado en username.
     *
     * @param username nombre de usuario
     * @return email en formato username@ondrasounds.com
     */
    private String generarEmail(String username) {
        return username.toLowerCase() + "@ondrasounds.com";
    }

    /**
     * Construye URL de red social según plataforma y username.
     *
     * @param plataforma nombre de la red social
     * @param username identificador del usuario
     * @return URL completa de perfil social
     */
    private String generarUrlRedSocial(String plataforma, String username) {
        switch (plataforma.toUpperCase()) {
            case "INSTAGRAM":
                return "https://instagram.com/" + username;
            case "X":
                return "https://twitter.com/" + username;
            case "FACEBOOK":
                return "https://facebook.com/" + username;
            case "TIKTOK":
                return "https://tiktok.com/@" + username;
            case "YOUTUBE":
                return "https://youtube.com/@" + username;
            case "SPOTIFY":
                return "https://open.spotify.com/artist/" + username;
            default:
                return "https://" + plataforma.toLowerCase() + ".com/" + username;
        }
    }

    /**
     * Genera IBAN español de prueba.
     * Formato ES + 22 dígitos aleatorios.
     *
     * @return IBAN ficticio
     */
    private String generarIBAN() {
        Random random = new Random();
        StringBuilder iban = new StringBuilder("ES");
        for (int i = 0; i < 22; i++) {
            iban.append(random.nextInt(10));
        }
        return iban.toString();
    }

    /**
     * Genera fecha de registro aleatoria dentro de los últimos 2 años.
     *
     * @return fecha entre 1 y 730 días atrás
     */
    private LocalDateTime generarFechaRegistro() {
        Random random = new Random();
        int diasAtras = random.nextInt(730) + 1;
        return LocalDateTime.now().minusDays(diasAtras);
    }

    /**
     * Genera fecha de inicio artístico aleatoria dentro de los últimos 5 años.
     *
     * @return fecha entre 1 y 1825 días atrás
     */
    private LocalDateTime generarFechaInicioArtistico() {
        Random random = new Random();
        int diasAtras = random.nextInt(1825) + 1;
        return LocalDateTime.now().minusDays(diasAtras);
    }

    /**
     * Crea relaciones de seguimiento aleatorias entre usuarios y artistas.
     *
     * @param usuariosNormales lista de usuarios que seguirán a otros
     * @return cantidad total de seguimientos creados
     */
    private int poblarSeguimientos(List<Usuario> usuariosNormales) {
        Random random = new Random();
        int totalSeguimientos = 0;

        List<Usuario> artistas = usuarioRepository.findAll().stream()
                .filter(u -> u.getTipoUsuario() == TipoUsuario.ARTISTA)
                .collect(Collectors.toList());

        log.debug("👥 {} usuarios normales disponibles", usuariosNormales.size());
        log.debug("🎵 {} artistas disponibles", artistas.size());

        for (Usuario seguidor : usuariosNormales) {
            int seguimientosCreados = 0;

            try {
                int cantidadArtistas = (int) (artistas.size() * (0.6 + random.nextDouble() * 0.2));
                List<Usuario> artistasSeleccionados = seleccionarAleatorios(artistas, cantidadArtistas, random);

                for (Usuario artista : artistasSeleccionados) {
                    if (crearSeguimiento(seguidor, artista)) {
                        seguimientosCreados++;
                    }
                }

                int cantidadUsuarios = (int) (usuariosNormales.size() * (0.3 + random.nextDouble() * 0.2));
                List<Usuario> usuariosSeleccionados = seleccionarAleatorios(usuariosNormales, cantidadUsuarios, random);

                for (Usuario seguido : usuariosSeleccionados) {
                    if (!seguido.getIdUsuario().equals(seguidor.getIdUsuario())) {
                        if (crearSeguimiento(seguidor, seguido)) {
                            seguimientosCreados++;
                        }
                    }
                }

                totalSeguimientos += seguimientosCreados;
                log.debug("  ✓ Usuario {} ahora sigue a {} perfiles",
                        seguidor.getNombreUsuario(), seguimientosCreados);

            } catch (Exception e) {
                log.warn("  ⚠ Error al crear seguimientos para {}: {}",
                        seguidor.getNombreUsuario(), e.getMessage());
            }
        }

        return totalSeguimientos;
    }

    /**
     * Crea una relación de seguimiento si no existe previamente.
     *
     * @param seguidor usuario que sigue
     * @param seguido usuario seguido
     * @return true si se creó el seguimiento, false si ya existía
     */
    private boolean crearSeguimiento(Usuario seguidor, Usuario seguido) {
        try {
            boolean existe = seguimientoRepository.existsBySeguidor_IdUsuarioAndSeguido_IdUsuario(
                    seguidor.getIdUsuario(),
                    seguido.getIdUsuario()
            );

            if (existe) {
                return false;
            }

            Seguimiento seguimiento = Seguimiento.builder()
                    .seguidor(seguidor)
                    .seguido(seguido)
                    .fechaSeguimiento(generarFechaSeguimiento())
                    .build();

            seguimientoRepository.save(seguimiento);
            return true;

        } catch (Exception e) {
            log.debug("No se pudo crear seguimiento: {} -> {}",
                    seguidor.getNombreUsuario(), seguido.getNombreUsuario());
            return false;
        }
    }

    /**
     * Selecciona elementos aleatorios de una lista sin modificar la original.
     *
     * @param lista lista origen
     * @param cantidad número de elementos a seleccionar
     * @param random generador de números aleatorios
     * @return sublista con elementos aleatorios
     */
    private <T> List<T> seleccionarAleatorios(List<T> lista, int cantidad, Random random) {
        if (cantidad >= lista.size()) {
            return new ArrayList<>(lista);
        }

        List<T> copia = new ArrayList<>(lista);
        Collections.shuffle(copia, random);
        return copia.subList(0, Math.min(cantidad, copia.size()));
    }

    /**
     * Genera fecha de seguimiento aleatoria dentro del último año.
     *
     * @return fecha entre 1 y 365 días atrás
     */
    private LocalDateTime generarFechaSeguimiento() {
        Random random = new Random();
        int diasAtras = random.nextInt(365) + 1;
        return LocalDateTime.now().minusDays(diasAtras);
    }

    /**
     * Genera número de tarjeta ficticio de 16 dígitos.
     *
     * @return número de tarjeta con formato XXXX XXXX XXXX XXXX
     */
    private String generarNumeroTarjeta() {
        Random random = new Random();
        StringBuilder numero = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            numero.append(random.nextInt(10));
            if ((i + 1) % 4 == 0 && i < 15) {
                numero.append(" ");
            }
        }
        return numero.toString();
    }

    /**
     * Genera fecha de caducidad futura en formato MM/YY.
     *
     * @return fecha entre 1 y 5 años en el futuro
     */
    private String generarFechaCaducidad() {
        Random random = new Random();
        int mes = random.nextInt(12) + 1;
        int año = LocalDateTime.now().getYear() + random.nextInt(5) + 1;
        return String.format("%02d/%02d", mes, año % 100);
    }

    /**
     * Genera número de teléfono español ficticio con formato +34 6XX XXX XXX.
     *
     * @return teléfono móvil español
     */
    private String generarTelefono() {
        Random random = new Random();
        return String.format("+34 %d%d%d %d%d%d %d%d%d",
                6 + random.nextInt(2),
                random.nextInt(10), random.nextInt(10),
                random.nextInt(10), random.nextInt(10), random.nextInt(10),
                random.nextInt(10), random.nextInt(10), random.nextInt(10)
        );
    }
}