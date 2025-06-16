package com.bitsealer.service;

import com.bitsealer.user.AppUser;
import com.bitsealer.repository.UserRepository;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ────────────  Inyección por constructor (recomendada) ────────────
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 
     * Registra un nuevo usuario en la base de datos.
     * 1) Valida que el e-mail NO exista.  
     * 2) Codifica la contraseña con BCrypt.  
     * 3) Asigna el rol por defecto si no viene informado.  
     */
    public AppUser registerUser(AppUser user) {

        // ── 1. Email único ─────────────────────────────────────────────
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El e-mail ya está registrado.");
        }

        // (Opcional) Comprobar username único
        userRepository.findByUsername(user.getUsername())
                .ifPresent(u -> { throw new IllegalArgumentException("El nombre de usuario ya está en uso."); });

        // ── 2. Codificar contraseña ───────────────────────────────────
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // ── 3. Rol por defecto ────────────────────────────────────────
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ROLE_USER");
        }

        // ── 4. Persistir y devolver ───────────────────────────────────
        return userRepository.save(user);
    }

    public Optional<AppUser> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
