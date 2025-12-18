package com.bitsealer.integration;

import com.bitsealer.model.AppUser;
import com.bitsealer.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Registro correcto devuelve 201 y JwtResponse")
    void register_Success() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", "alice");
        body.put("email", "alice@example.com");
        body.put("password", "secret123");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("alice@example.com"));

        AppUser user = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(passwordEncoder.matches("secret123", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Registro con email duplicado devuelve 409")
    void register_DuplicateEmail_Returns409() throws Exception {
        AppUser existing = new AppUser();
        existing.setUsername("bob");
        existing.setEmail("alice@example.com");
        existing.setPassword("pass");
        existing.setRole("ROLE_USER");
        userRepository.save(existing);

        Map<String, String> body = new HashMap<>();
        body.put("name", "alice");
        body.put("email", "alice@example.com");
        body.put("password", "secret123");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Registro con datos inválidos devuelve 400 + errores de validación")
    void register_InvalidPayload_Returns400WithErrors() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", "");
        body.put("email", "not-an-email");
        body.put("password", "123");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").isNotEmpty())
                .andExpect(jsonPath("$.errors.email").isNotEmpty())
                .andExpect(jsonPath("$.errors.password").isNotEmpty());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Login correcto devuelve 200 + JwtResponse")
    void login_Success() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword(passwordEncoder.encode("secret123"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        Map<String, String> body = new HashMap<>();
        body.put("email", "alice@example.com");
        body.put("password", "secret123");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Login con datos inválidos devuelve 400 + errores de validación")
    void login_InvalidPayload_Returns400WithErrors() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "");
        body.put("password", "");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").isNotEmpty())
                .andExpect(jsonPath("$.errors.password").isNotEmpty());
    }

    @Test
    @DisplayName("Login con credenciales erróneas devuelve 401")
    void login_WrongCredentials_Returns401() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword(passwordEncoder.encode("secret123"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        Map<String, String> body = new HashMap<>();
        body.put("email", "alice@example.com");
        body.put("password", "wrongpass");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
