package com.bitsealer.controller;

import com.bitsealer.dto.UserDto;
import com.bitsealer.model.AppUser;
import com.bitsealer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Devuelve los datos del usuario autenticado (perfil).
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyUser(Authentication authentication) {
        String username = authentication.getName();
        AppUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(null);
        }
        UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(dto);
    }
}
