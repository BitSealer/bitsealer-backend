package com.bitsealer.controller;

import com.bitsealer.dto.UpdatePasswordRequest;
import com.bitsealer.dto.UserDto;
import com.bitsealer.model.AppUser;
import com.bitsealer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Devuelve los datos del usuario autenticado (perfil).
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyUser(Authentication authentication) {
        String username = authentication.getName();
        AppUser user = userService.getByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(null);
        }

        UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updateMyPassword(Authentication authentication,
                                              @Valid @RequestBody UpdatePasswordRequest request) {
        try {
            String username = authentication.getName();
            userService.updatePassword(username, request.currentPassword(), request.newPassword());
            return ResponseEntity.ok("Contraseña actualizada correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}