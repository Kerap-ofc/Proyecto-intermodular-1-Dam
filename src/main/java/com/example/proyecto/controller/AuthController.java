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
public class AuthController {

    @Autowired
    private UserRepository userRepository;

@GetMapping({"/reservas", "/perfil"})
public String revisarReservas(
        @RequestParam(name = "buscar", required = false, defaultValue = "") String buscar,
        @RequestParam(name = "orden", required = false, defaultValue = "desc") String orden,
        @RequestParam(name = "filtro", required = false, defaultValue = "todos") String filtro,
        HttpSession session, Model model) {
    
    // Candado de sesión original de tu GitHub
    User usuarioLogueado = (User) session.getAttribute("usuarioSesion");
    if (usuarioLogueado == null) {
        return "login"; 
    }
    model.addAttribute("usuario", usuarioLogueado);

    // Listas vacías listas para llenarse con los datos filtrados de MySQL
    List<Map<String, Object>> libros = new java.util.ArrayList<>();
    List<Map<String, Object>> musica = new java.util.ArrayList<>();

    Integer userId = usuarioLogueado.getId();

    // FILTRAR: Dependiendo de lo que pulse el usuario, llamamos a tus nuevas consultas de buscar y ordenar
    if (filtro.equals("todos") || filtro.equals("libros")) {
        libros = userRepository.findBookReservations(userId, buscar, orden);
    }
    
    if (filtro.equals("todos") || filtro.equals("musica")) {
        musica = userRepository.findMusicReservations(userId, buscar, orden);
    }

    // Enviamos los resultados limpios a las tarjetas de Thymeleaf
    model.addAttribute("libros", libros);
    model.addAttribute("musica", musica);

    // DEVOLVEMOS LOS ESTADOS: Esto es vital para que el HTML sepa qué botón está pulsado
    model.addAttribute("buscarActual", buscar);
    model.addAttribute("ordenActual", orden);
    model.addAttribute("filtroActual", filtro);

    return "perfil-usuario";
}

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String username, 
                                @RequestParam String password, 
                                HttpSession session, 
                                Model model) {
        
        // Buscamos al usuario en la base de datos de Docker por su username
        Optional<User> userOpt = userRepository.findByUsername(username);

        // Comprobamos si el usuario existe y si la contraseña de la BD coincide con la escrita
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            // ¡Éxito! Guardamos el usuario entero en la sesión para recordar que está dentro
            session.setAttribute("usuarioSesion", userOpt.get());
            return "redirect:/"; // Lo redirigimos de vuelta a la página principal (Principio.html)
        }

        // Si los datos son incorrectos, volvemos a cargar el login con un mensaje de error
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    // 3. Por si en el futuro quieres poner un botón de "Cerrar Sesión"
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Destruye la sesión actual del navegador
        return "redirect:/";
    }
}