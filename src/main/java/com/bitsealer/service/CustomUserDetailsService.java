package com.bitsealer.service;

import com.bitsealer.user.AppUser;
import com.bitsealer.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Inyección por constructor (recomendada)
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Spring Security llama a este método cuando alguien intenta iniciar sesión.
     * Permitimos que el usuario se identifique con su nombre de usuario **o** su email.
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        AppUser user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario o email no encontrado: " + usernameOrEmail));

        // Convertimos nuestro AppUser en la implementación que Spring Security entiende
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),                      // nombre que se mostrará
                user.getPassword(),                      // contraseña cifrada
                List.of(new SimpleGrantedAuthority(user.getRole())) // roles/autoridades
        );
    }
}
