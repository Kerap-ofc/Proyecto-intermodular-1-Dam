package com.example.proyecto.controller;

import com.example.proyecto.entity.User;
import com.example.proyecto.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // 1. Carga la página principal de los vídeos (Sifitecha)
    @GetMapping("/")
    public String home() {
        return "Principio"; // Busca tu archivo templates/Principio.html
    }

    // 1. Carga la pantalla verde con la lista de libros de MySQL
// ========================================================
    // SECCIÓN TOTALMENTE NUEVA: PÁGINA DE LIBROS Y RESERVAS
    // ========================================================

    // 1. Carga la pantalla verde con la lista de libros y las fechas de este usuario
@GetMapping("/libros")
    public String mostrarPaginaLibros(HttpSession session, Model model) {
        // Candado de seguridad obligatorio
        User usuarioActivo = (User) session.getAttribute("usuarioSesion");
        if (usuarioActivo == null) {
            return "redirect:/login";
        }

        try {
            // SÚPER SIMPLE: Traemos solo los libros reales de la tabla 'books' sin duplicados
            String sql = "SELECT * FROM books";
            java.util.List<java.util.Map<String, Object>> listaLibros = jdbcTemplate.queryForList(sql);
            
            model.addAttribute("libros", listaLibros);
            model.addAttribute("usuarioNombre", usuarioActivo.getUsername());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar libros: " + e.getMessage());
        }

        return "libros";
    }

    // 2. Registra la descarga en la base de datos y despacha el archivo PDF
   @GetMapping("/descargar-libro")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> descargarYReservar(
            @RequestParam("id") Long bookId, HttpSession session) {
        
        User usuarioActivo = (User) session.getAttribute("usuarioSesion");
        if (usuarioActivo == null) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 1. Insertar la reserva (Ya funciona perfecto en tu base de datos)
            java.time.LocalDateTime fechaActual = java.time.LocalDateTime.now();
            String sqlReserva = "INSERT INTO book_reservations (user_id, book_id, reservation_date) VALUES (?, ?, ?)";
            jdbcTemplate.update(sqlReserva, usuarioActivo.getId(), bookId, fechaActual);

            // 2. Buscar la ruta guardada en la base de datos (Ej: /uploads/pdfs/archivo.pdf)
            String sqlFichero = "SELECT pdf_path FROM books WHERE id = ?";
            String pdfPathBd = jdbcTemplate.queryForObject(sqlFichero, String.class, bookId);

            // 3. Extraemos el nombre del archivo para buscarlo en la carpeta física real
            String nombreFichero = pdfPathBd.substring(pdfPathBd.lastIndexOf("/") + 1);
            
            // Apuntamos a la carpeta física real "uploads/libros/" donde se guardan tus PDFs
            java.nio.file.Path rutaArchivoFisico = java.nio.file.Paths.get("uploads/libros").resolve(nombreFichero).normalize();
            org.springframework.core.io.Resource recurso = new org.springframework.core.io.UrlResource(rutaArchivoFisico.toUri());

            if (!recurso.exists()) {
                // Si el archivo no existe físicamente en la carpeta, nos avisa con un error de servidor limpio
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            // 4. Despachamos el archivo PDF forzando la descarga directa en el navegador
            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(recurso);

        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ========================================================
    // SECCIÓN EXACTAMENTE IGUAL PERO PARA MÚSICA
    // ========================================================

    // 1. Muestra la pantalla con la lista de canciones únicas de la tabla 'music'
    @GetMapping("/musica")
    public String mostrarPaginaMusica(HttpSession session, Model model) {
        // Candado de seguridad obligatorio
        User usuarioActivo = (User) session.getAttribute("usuarioSesion");
        if (usuarioActivo == null) {
            return "redirect:/login";
        }

        try {
            // SÚPER SIMPLE: Traemos solo las canciones reales de la tabla 'music' sin duplicados
            String sql = "SELECT * FROM music";
            java.util.List<java.util.Map<String, Object>> listaMusica = jdbcTemplate.queryForList(sql);
            
            model.addAttribute("canciones", listaMusica);
            model.addAttribute("usuarioNombre", usuarioActivo.getUsername());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la música: " + e.getMessage());
        }

        return "musica"; // Buscará el archivo templates/musica.html
    }

    // 2. Registra la descarga en 'music_reservations' y despacha el archivo de audio
    @GetMapping("/descargar-musica")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> descargarYReservarMusica(
            @RequestParam("id") Long musicId, HttpSession session) {
        
        User usuarioActivo = (User) session.getAttribute("usuarioSesion");
        if (usuarioActivo == null) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 1. Insertar la reserva en la tabla de música con las columnas correctas
            java.time.LocalDateTime fechaActual = java.time.LocalDateTime.now();
            String sqlReserva = "INSERT INTO music_reservations (user_id, music_id, reservation_date) VALUES (?, ?, ?)";
            jdbcTemplate.update(sqlReserva, usuarioActivo.getId(), musicId, fechaActual);

            // 2. Buscar la ruta del archivo guardada en la base de datos (asumiendo columna mp3_path o similar, ajústala si se llama music_path)
            String sqlFichero = "SELECT mp3_path FROM music WHERE id = ?";
            String mp3PathBd = jdbcTemplate.queryForObject(sqlFichero, String.class, musicId);

            // 3. Extraemos el nombre del archivo para buscarlo en tu carpeta física
            String nombreFichero = mp3PathBd.substring(mp3PathBd.lastIndexOf("/") + 1);
            
            // Apuntamos a la carpeta física real "uploads/musica" (o como la tengas estructurada)
            java.nio.file.Path rutaArchivoFisico = java.nio.file.Paths.get("uploads/musica").resolve(nombreFichero).normalize();
            org.springframework.core.io.Resource recurso = new org.springframework.core.io.UrlResource(rutaArchivoFisico.toUri());

            if (!recurso.exists()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            // 4. Despachamos el archivo forzando la descarga directa en el navegador
            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType("audio/mpeg"))
                    .body(recurso);

        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 2. Muestra la pantalla del formulario de login cuando te echa un candado
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login"; // Busca templates/login.html
    }

    // 3. PROCESA EL LOGIN: Comprueba el usuario en la BD de Docker
    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session, Model model) {

        Optional<User> usuarioDb = userRepository.findByUsername(username);

        if (usuarioDb.isPresent() && usuarioDb.get().getPassword().equals(password)) {
            // ¡LOGUEADO CON ÉXITO! Guardamos el usuario en la sesión del navegador
            session.setAttribute("usuarioSesion", usuarioDb.get());
            return "redirect:/reservas";
        }

        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }

    // 4. Muestra la nueva pantalla de registro que me has pedido
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro"; // Busca templates/registro.html
    }

    // 5. PROCESA EL REGISTRO: Guarda el nuevo usuario en MySQL (Docker)
    // PROCESA EL REGISTRO: Adaptado al formulario real de 5 campos
    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("nombre") String nombre,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("email") String email,
            Model model) {

        // Verificamos si el username ya existe en tu BD para no duplicarlo
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya está registrado");
            return "registro";
        }

        // Creamos la entidad mapeando los setters reales de tu objeto User
        User nuevoUsuario = new User();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(password);

        // NOTA: Si en tu entidad User los atributos se llaman diferente (ej: firstName,
        // lastName),
        // cambia aquí el "setNombre" por el nombre exacto de tu setter.
        nuevoUsuario.setFirstname(nombre);
        nuevoUsuario.setLastname(apellidos);
        nuevoUsuario.setEmail(email);

        userRepository.save(nuevoUsuario);

        // Si todo sale bien, lo mandamos al login para que entre de forma segura
        return "redirect:/login";
    }
    // ========================================================
    // SECCIÓN DE LIBROS (BLINDADA CON CANDADO DE SESIÓN)
    // ========================================================

    // 1. Muestra el formulario solo si estás logueado
    @GetMapping("/subir-libro")
    public String mostrarSubirLibro(HttpSession session) {
        if (session.getAttribute("usuarioSesion") == null) {
            return "redirect:/login"; // Candado activado: al login de cabeza
        }
        return "subir-libro";
    }

    // 2. Procesa la subida solo si estás logueado
    @PostMapping("/recursos/subir-libro")
    public String procesarSubirLibro(
            @RequestParam("titulo") String title, // Mapeado a la columna 'title'
            @RequestParam("genero") String genre, // Mapeado a la columna 'genre'
            @RequestParam("autor") String author, // Mapeado a la columna 'author'
            @RequestParam("fechaLanzamiento") String release_year, // Mapeado a 'release_year'
            @RequestParam("descripcion") String description, // Mapeado a 'description'
            @RequestParam("libroPdf") org.springframework.web.multipart.MultipartFile pdf_path, // El archivo PDF
            HttpSession session, Model model) {

        if (session.getAttribute("usuarioSesion") == null) {
            return "redirect:/login";
        }

        if (pdf_path.isEmpty() || !pdf_path.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            model.addAttribute("error", "Error: El archivo debe ser un PDF válido.");
            return "subir-libro";
        }

        try {
            String nombreArchivo = pdf_path.getOriginalFilename();
            // Creamos la ruta exacta que guarda tu base de datos para los PDFs
            String rutaCompletaBd = "/uploads/pdfs/" + nombreArchivo;

            // 1. Guardar archivo físico en su carpeta correspondiente
            byte[] bytes = pdf_path.getBytes();
            java.nio.file.Path path = java.nio.file.Paths.get("uploads/libros/" + nombreArchivo);
            java.nio.file.Files.write(path, bytes);

            // 2. Insertar en tu tabla real 'books' de Docker con las columnas en inglés
            String sql = "INSERT INTO books (title, genre, pdf_path, author, release_year, description) VALUES (?, ?, ?, ?, ?, ?)";

            // Ejecutamos pasándole las variables en el orden exacto de la consulta SQL
            jdbcTemplate.update(sql, title, genre, rutaCompletaBd, author, release_year, description);

            model.addAttribute("exito", "¡Libro guardado y PDF enlazado correctamente!");

            // 3. ¡Mágia! Redirigimos directamente a la página principal tras subirlo con
            // éxito
            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar en la BD: " + e.getMessage());
            return "subir-libro";
        }
    }

    // ========================================================
    // SECCIÓN DE MÚSICA (BLINDADA CON CANDADO DE SESIÓN)
    // ========================================================

    // 3. Muestra el formulario solo si estás logueado
    @GetMapping("/subir-musica")
    public String mostrarSubirMusica(HttpSession session) {
        if (session.getAttribute("usuarioSesion") == null) {
            return "redirect:/login"; // Candado activado: al login de cabeza
        }
        return "subir-musica";
    }

    // 4. Procesa la subida solo si estás logueado
    @PostMapping("/recursos/subir-musica")
    public String procesarSubirMusica(
            @RequestParam("titulo") String title,
            @RequestParam("genero") String genre,
            @RequestParam("autor") String artist_or_band,
            @RequestParam("fechaLanzamiento") String release_year,
            @RequestParam("album") String album,
            @RequestParam("musicaMp3") org.springframework.web.multipart.MultipartFile mp3_path,
            HttpSession session, Model model) {

        // Candado doble
        if (session.getAttribute("usuarioSesion") == null) {
            return "redirect:/login";
        }

        if (mp3_path.isEmpty() || !mp3_path.getOriginalFilename().toLowerCase().endsWith(".mp3")) {
            model.addAttribute("error", "Error: El archivo debe ser un MP3 válido.");
            return "subir-musica";
        }

        try {
            String nombreArchivo = mp3_path.getOriginalFilename();
            // Creamos la ruta exacta que espera tu base de datos
            String rutaCompletaBd = "/uploads/mp3s/" + nombreArchivo;

            // Guardar archivo físico
            byte[] bytes = mp3_path.getBytes();
            java.nio.file.Path path = java.nio.file.Paths.get("uploads/musica/" + nombreArchivo);
            java.nio.file.Files.write(path, bytes);

            // Insertar en tu tabla 'music' de Docker
            String sql = "INSERT INTO music (title, genre, artist_or_band, release_year, album, mp3_path) VALUES (?, ?, ?, ?, ?, ?)";

            // CAMBIO AQUÍ: Cambiamos 'nombreArchivo' por 'rutaCompletaBd' al final
            jdbcTemplate.update(sql, title, genre, artist_or_band, release_year, album, rutaCompletaBd);

            model.addAttribute("exito", "¡Música guardada y MP3 enlazado correctamente!");
            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar en la BD: " + e.getMessage());
            return "subir-musica";
        }
    }
    // ========================================================
    // RUTA PRINCIPAL PANEL DE REGISTROS (EXCLUSIVO ADMIN)
    // ========================================================
    @GetMapping("/admin/registros")
    public String abrirPanelRegistros(HttpSession session, Model model) {
        // Candado de seguridad para que nadie pueda saltarse la URL
        User usuarioActivo = (User) session.getAttribute("usuarioSesion");
        if (usuarioActivo == null || !usuarioActivo.getIsAdmin()) {
            return "redirect:/login";
        }

        // 1. Consultamos todos los libros existentes (únicos)
        List<Map<String, Object>> libros = jdbcTemplate.queryForList("SELECT * FROM books");
        
        // 2. Consultamos todas las canciones existentes (únicas)
        List<Map<String, Object>> canciones = jdbcTemplate.queryForList("SELECT * FROM music");
        
        // 3. Consultamos todos los usuarios (excepto el administrador logueado actual para no autoborrarse)
        List<Map<String, Object>> usuarios = jdbcTemplate.queryForList("SELECT id, username, firstname, lastname, email FROM users WHERE id != ?", usuarioActivo.getId());

        // Enviamos las 3 colecciones completas a la plantilla
        model.addAttribute("libros", libros);
        model.addAttribute("canciones", canciones);
        model.addAttribute("usuarios", usuarios);

        return "registros"; // Devuelve registros.html
    }

    // ========================================================
    // ACCIONES POST DE ELIMINACIÓN DEFINITIVA EN BD
    // ========================================================

    @PostMapping("/admin/borrar-libro")
    public String adminBorrarLibro(@RequestParam("id") Long id, HttpSession session) {
        User usuarioActivo = (User) session.getAttribute("usuarioSesion");
        if (usuarioActivo == null || !usuarioActivo.getIsAdmin()) return "redirect:/login";

        // Limpieza en cascada manual de registros descargados para evitar fallos de Foreign Key
        jdbcTemplate.update("DELETE FROM book_reservations WHERE book_id = ?", id);
        jdbcTemplate.update("DELETE FROM books WHERE id = ?", id);
        
        return "redirect:/admin/registros";
    }

    @PostMapping("/admin/borrar-musica")
    public String adminBorrarMusica(@RequestParam("id") Long id, HttpSession session) {
        User usuarioActivo = (User) session.getAttribute("usuarioSesion");
        if (usuarioActivo == null || !usuarioActivo.getIsAdmin()) return "redirect:/login";

        // Limpieza en cascada manual de descargas de música
        jdbcTemplate.update("DELETE FROM music_reservations WHERE music_id = ?", id);
        jdbcTemplate.update("DELETE FROM music WHERE id = ?", id);
        
        return "redirect:/admin/registros";
    }

    @PostMapping("/admin/borrar-usuario")
    public String adminBorrarUsuario(@RequestParam("id") Long id, HttpSession session) {
        User usuarioActivo = (User) session.getAttribute("usuarioSesion");
        if (usuarioActivo == null || !usuarioActivo.getIsAdmin()) return "redirect:/login";

        // Limpieza de todos los historiales asociados al usuario antes de borrarlo de la tabla users
        jdbcTemplate.update("DELETE FROM book_reservations WHERE user_id = ?", id);
        jdbcTemplate.update("DELETE FROM music_reservations WHERE user_id = ?", id);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        
        return "redirect:/admin/registros";
    }

    @GetMapping("/contacto")
    public String mostrarContacto() {
        return "contacto"; // Busca templates/contacto.html
    }

}