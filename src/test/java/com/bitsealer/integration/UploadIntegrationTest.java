package com.bitsealer.integration;

import com.bitsealer.model.AppUser;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UploadIntegrationTest {

    /* 1) Contenedor PostgreSQL aislado */
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14-alpine")
                    .withDatabaseName("bitsealertest")
                    .withUsername("test")
                    .withPassword("test");

    /* 2) Inyectar propiedades en Spring */
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",      postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    /* 3) Beans necesarios */
    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;
    @Autowired FileHashRepository hashRepo;

    @Test
    @WithMockUser(username = "alice")     // usuario simulado al hacer login
    void uploadEndpoint_persistsHashAndRedirects() throws Exception {

        /* a) Crear usuario de prueba en la BD */
        AppUser alice = new AppUser();
        alice.setUsername("alice");
        alice.setEmail("alice@example.com");   // email es NOT NULL en tu entidad
        alice.setPassword("x");                // password en claro (sólo para test)
        userRepo.save(alice);

        /* b) Contar hashes antes de la llamada */
        long antes = hashRepo.count();

        /* c) Archivo de ejemplo */
        MockMultipartFile file = new MockMultipartFile(
                "file", "hola.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hola mundo".getBytes()
        );

        /* d) Llamar al endpoint /upload */
        mvc.perform(multipart("/upload").file(file))
           .andExpect(redirectedUrl("/history"));   // <-- matcher fijo

        /* e) Verificar que se añadió exactamente un registro */
        long despues = hashRepo.count();
        assertThat(despues).isEqualTo(antes + 1);
    }
}
