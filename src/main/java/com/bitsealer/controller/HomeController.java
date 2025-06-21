package com.bitsealer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Muestra la página de inicio.
     *  - “/”  ➜ home.html (accesible para cualquiera)
     *  - “/home” (por legibilidad, redirige a "/")
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Puedes enviar datos a la vista, por ejemplo la fecha actual:
        // model.addAttribute("today", LocalDate.now());
        return "home";      // -> templates/home.html
    }
}
