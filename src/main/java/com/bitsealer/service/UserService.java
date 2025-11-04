package com.bitsealer.service;

import com.bitsealer.model.AppUser;
import com.bitsealer.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Guarda un nuevo usuario aplicando validaciones:
     * - Email único
     * - Username único
     * Contraseña será cifrada antes de guardar.
     * @throws IllegalArgumentException si email o username ya existen.
     */
    public AppUser save(AppUser user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso.");
        }
        userRepository.findByUsername(user.getUsername())
            .ifPresent(u -> { throw new IllegalArgumentException("El nombre de usuario ya está en uso."); });
        // Cifrar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Rol por defecto
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }
        return userRepository.save(user);
    }

    public Optional<AppUser> getByEmailOrUsername(String emailOrUsername) {
        return userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername));
    }

    public Optional<AppUser> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<AppUser> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
