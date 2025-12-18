package com.bitsealer.integration;

import com.bitsealer.model.AppUser;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UploadIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private FileHashRepository hashRepo;

    @BeforeEach
    void setUp() {
        // Limpieza para test repetible
        hashRepo.deleteAll();
        userRepo.deleteAll();

        // Creamos el usuario que usará @WithMockUser
        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setEmail("alice@test.local"); // ✅ OBLIGATORIO (NOT NULL)
        user.setPassword("test");          // da igual en este test

        // ✅ Si tu entidad tiene role NOT NULL, ponlo también:
        // Si AppUser.role es enum: user.setRole(Role.ROLE_USER);
        // Si es String:          user.setRole("ROLE_USER");
        // (elige UNA de las dos líneas según tu tipo real)
        user.setRole("ROLE_USER");

        userRepo.save(user);
    }

    @Test
    @WithMockUser(username = "alice")
    void uploadEndpoint_persistsHashAndReturns201() throws Exception {

        long antes = hashRepo.count();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hola.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hola mundo".getBytes()
        );

        mvc.perform(
                multipart("/api/files/upload")
                        .file(file)
                        .with(csrf())
        ).andExpect(status().isCreated());

        long despues = hashRepo.count();
        assertThat(despues).isEqualTo(antes + 1);
    }
}
