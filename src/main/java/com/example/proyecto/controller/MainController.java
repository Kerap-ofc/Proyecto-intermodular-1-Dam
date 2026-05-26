package com.example.proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // Este método soluciona el error 404 de la raíz
    @GetMapping("/")
    public String home() {
        return "Principio"; // Busca tu archivo templates/Principio.html
    }
}