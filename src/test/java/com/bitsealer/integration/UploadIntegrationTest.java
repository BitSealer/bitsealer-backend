package com.bitsealer.integration;

import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.UserRepository;
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
    MockMvc mvc;

    @Autowired
    UserRepository userRepo;

    @Autowired
    FileHashRepository hashRepo;

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
                        .with(csrf())   // por si CSRF está activo
        ).andExpect(status().isCreated());

        long despues = hashRepo.count();
        assertThat(despues).isEqualTo(antes + 1);
    }
}
