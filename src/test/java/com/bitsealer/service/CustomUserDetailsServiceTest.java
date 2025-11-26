package com.bitsealer.service;

import com.bitsealer.model.AppUser;
import com.bitsealer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setUsername("alice");
        appUser.setEmail("alice@example.com");
        appUser.setPassword("encoded-password");
        appUser.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername: devuelve UserDetails cuando encuentra por username")
    void loadUserByUsername_FoundByUsername() {
        given(userRepository.findByUsername("alice")).willReturn(Optional.of(appUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("alice");

        assertThat(userDetails.getUsername()).isEqualTo("alice");
        assertThat(userDetails.getPassword()).isEqualTo("encoded-password");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername: busca por email si no encuentra por username")
    void loadUserByUsername_FoundByEmail() {
        given(userRepository.findByUsername("alice@example.com")).willReturn(Optional.empty());
        given(userRepository.findByEmail("alice@example.com")).willReturn(Optional.of(appUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("alice@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("alice");
        assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    @DisplayName("loadUserByUsername: lanza UsernameNotFoundException si no existe ni username ni email")
    void loadUserByUsername_NotFound_ThrowsException() {
        given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());
        given(userRepository.findByEmail("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario o email no encontrado: unknown");
    }
}
