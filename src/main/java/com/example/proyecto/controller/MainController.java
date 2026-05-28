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
            @RequestParam("titulo") String title,             // Mapeado a la columna 'title'
            @RequestParam("genero") String genre,             // Mapeado a la columna 'genre'
            @RequestParam("autor") String author,             // Mapeado a la columna 'author'
            @RequestParam("fechaLanzamiento") String release_year, // Mapeado a 'release_year'
            @RequestParam("descripcion") String description,   // Mapeado a 'description'
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
            
            // 3. ¡Mágia! Redirigimos directamente a la página principal tras subirlo con éxito
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
}