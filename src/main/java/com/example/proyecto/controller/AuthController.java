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
public class AuthController {

    @Autowired
    private UserRepository userRepository;

@GetMapping("/perfil")
public String revisarPerfil(HttpSession session) {
    // Forzamos el return temporalmente para ver si tu login.html responde
    return "login"; 
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