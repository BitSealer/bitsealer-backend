package com.bitsealer.integration;

import com.bitsealer.dto.StamperStampResponse;
import com.bitsealer.model.AppUser;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.UserRepository;
import com.bitsealer.service.StamperClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

    @MockBean
    private StamperClient stamperClient;

    @BeforeEach
    void setUp() {
        hashRepo.deleteAll();
        userRepo.deleteAll();

        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setEmail("alice@test.local");
        user.setPassword("test");
        user.setRole("ROLE_USER");

        userRepo.save(user);

        String fakeOtsProofB64 = Base64.getEncoder()
                .encodeToString("fake-ots-proof".getBytes(StandardCharsets.UTF_8));

        given(stamperClient.stamp(anyLong(), anyString(), anyString(), any(byte[].class)))
                .willReturn(new StamperStampResponse(
                        1L,
                        "ANCHORING",
                        fakeOtsProofB64,
                        "tx-test-123"
                ));
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

        then(stamperClient).should()
                .stamp(anyLong(), anyString(), anyString(), any(byte[].class));
    }
}