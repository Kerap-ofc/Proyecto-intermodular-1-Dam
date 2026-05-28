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
        
        // NOTA: Si en tu entidad User los atributos se llaman diferente (ej: firstName, lastName), 
        // cambia aquí el "setNombre" por el nombre exacto de tu setter.
        nuevoUsuario.setFirstname(nombre);
        nuevoUsuario.setLastname(apellidos);
        nuevoUsuario.setEmail(email);

        userRepository.save(nuevoUsuario);

        // Si todo sale bien, lo mandamos al login para que entre de forma segura
        return "redirect:/login";
    }
}