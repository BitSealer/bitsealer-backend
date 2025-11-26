package com.bitsealer.integration;

import com.bitsealer.model.AppUser;
import com.bitsealer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/users/me cuando el usuario existe")
    @WithMockUser(username = "alice")
    void getMe_Success() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("password");
        user.setRole("ROLE_USER");
        userRepository.save(user);

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /api/users/me cuando el usuario no existe devuelve 404")
    @WithMockUser(username = "ghost")
    void getMe_NotFound() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound());
    }
}
