package com.bitsealer.controller;

import com.bitsealer.dto.UserDto;
import com.bitsealer.model.AppUser;
import com.bitsealer.dto.LoginRequest;
import com.bitsealer.dto.RegisterRequest;
import com.bitsealer.dto.JwtResponse;
import com.bitsealer.security.jwt.JwtUtils;
import com.bitsealer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            // Crear nuevo usuario
            AppUser newUser = new AppUser();
            newUser.setUsername(request.name());
            newUser.setEmail(request.email());
            newUser.setPassword(request.password());
            AppUser saved = userService.save(newUser); // aplica encoder interno y guarda
            // Generar tokens JWT para el nuevo usuario
            String accessToken = jwtUtils.generateAccessToken(saved.getEmail());
            String refreshToken = jwtUtils.generateRefreshToken(saved.getEmail());
            UserDto userData = new UserDto(saved.getId(), saved.getUsername(), saved.getEmail());
            JwtResponse jwtResp = new JwtResponse(accessToken, refreshToken, userData);
            return ResponseEntity.status(201).body(jwtResp);
        } catch (IllegalArgumentException e) {
            // Este error lo lanza UserService si email/username duplicados
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error registrando usuario");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        // Buscar usuario por email o username
        AppUser user = userService.getByEmailOrUsername(request.email())
                        .orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
        // Generar JWT si credenciales válidas
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
        UserDto userData = new UserDto(user.getId(), user.getUsername(), user.getEmail());
        JwtResponse jwtResp = new JwtResponse(accessToken, refreshToken, userData);
        return ResponseEntity.ok(jwtResp);
    }
}
