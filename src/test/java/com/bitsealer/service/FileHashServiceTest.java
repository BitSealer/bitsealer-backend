/*package com.bitsealer.service;

import com.bitsealer.mapper.FileHashMapper;
import com.bitsealer.model.AppUser;
import com.bitsealer.model.FileHash;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FileHashServiceTest {

    private FileHashRepository hashRepo;
    private UserRepository     userRepo;
    private FileHashService    service;

    @BeforeEach
    void setUp() {
        hashRepo = mock(FileHashRepository.class);
        userRepo = mock(UserRepository.class);

        // Mapper real: solo convierte datos, no necesita mock
        FileHashMapper mapper = new FileHashMapper();

        // Constructor actualizado (repo, repo, mapper)
        service = new FileHashService(hashRepo, userRepo, mapper);
    }

    @Test
    void saveForUser_calculatesShaAndPersists() throws Exception {
        // ── 1) Usuario de prueba (sin setId, JPA lo gestiona en producción) ──
        AppUser alice = new AppUser();
        alice.setUsername("alice");
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));

        // ── 2) Archivo simulado ──
        byte[] content = "hola mundo".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file", "prueba.txt", "text/plain", content);

        // ── 3) Ejecutar servicio ──
        service.saveForUser(alice, file);

        // ── 4) Capturar lo que se guardó ──
        ArgumentCaptor<FileHash> captor = ArgumentCaptor.forClass(FileHash.class);
        verify(hashRepo).save(captor.capture());

        FileHash saved = captor.getValue();
        String expectedSha = DigestUtils.sha256Hex(content);

        // ── 5) Comprobaciones ──
        assertThat(saved.getSha256()).isEqualTo(expectedSha);
        assertThat(saved.getFileName()).isEqualTo("prueba.txt");
        assertThat(saved.getOwner()).isEqualTo(alice);
    }
}
*/