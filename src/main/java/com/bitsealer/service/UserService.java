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

        user.setPassword(passwordEncoder.encode(user.getPassword()));

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

    public AppUser getRequiredByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    public void updatePassword(String username, String currentPassword, String newPassword) {
        AppUser user = getRequiredByUsername(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}