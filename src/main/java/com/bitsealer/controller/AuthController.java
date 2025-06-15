package com.bitsealer.controller;

import com.bitsealer.user.AppUser;
import com.bitsealer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controla login y registro de usuarios.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /* ─────────  LOGIN ───────── */

    /** GET /login → muestra login.html */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /* ───────── REGISTRO ───────── */

    /** GET /register → muestra register.html */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userForm", new AppUser());
        return "register";
    }

    /** POST /register → procesa el formulario */
    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("userForm") AppUser userForm,
            BindingResult result,
            Model model) {

        // Validación de campos (si añades anotaciones en AppUser)
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(userForm);   // guarda en BD
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }

        // Todo OK ➜ volvemos al login con aviso “registered”
        return "redirect:/login?registered";
    }
}
