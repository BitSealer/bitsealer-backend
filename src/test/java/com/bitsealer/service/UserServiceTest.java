package com.bitsealer.service;

import com.bitsealer.model.AppUser;
import com.bitsealer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("plain-password");
        user.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("save: guarda usuario nuevo cifrando la contraseña")
    void save_Success_EncodesPasswordAndPersists() {
        given(userRepository.existsByEmail("alice@example.com")).willReturn(false);
        given(userRepository.findByUsername("alice")).willReturn(Optional.empty());
        given(passwordEncoder.encode("plain-password")).willReturn("encoded-password");
        given(userRepository.save(user)).willReturn(user);

        AppUser saved = userService.save(user);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser persisted = captor.getValue();

        assertThat(persisted.getPassword()).isEqualTo("encoded-password");
        assertThat(saved).isEqualTo(persisted);
    }

    @Test
    @DisplayName("save: lanza IllegalArgumentException si el email ya existe")
    void save_EmailAlreadyExists_ThrowsException() {
        given(userRepository.existsByEmail("alice@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.save(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está en uso.");
    }

    @Test
    @DisplayName("save: lanza IllegalArgumentException si el username ya existe")
    void save_UsernameAlreadyExists_ThrowsException() {
        given(userRepository.existsByEmail("alice@example.com")).willReturn(false);
        given(userRepository.findByUsername("alice")).willReturn(Optional.of(new AppUser()));

        assertThatThrownBy(() -> userService.save(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre de usuario ya está en uso.");
    }

    @Test
    @DisplayName("getByEmailOrUsername: cuando el correo existe, lo devuelve")
    void getByEmailOrUsername_FoundByEmail() {
        given(userRepository.findByEmail("alice@example.com")).willReturn(Optional.of(user));

        Optional<AppUser> result = userService.getByEmailOrUsername("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    @DisplayName("getByEmailOrUsername: cuando no existe por email pero sí por username, lo devuelve")
    void getByEmailOrUsername_FoundByUsername() {
        given(userRepository.findByEmail("alice")).willReturn(Optional.empty());
        given(userRepository.findByUsername("alice")).willReturn(Optional.of(user));

        Optional<AppUser> result = userService.getByEmailOrUsername("alice");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }
}
