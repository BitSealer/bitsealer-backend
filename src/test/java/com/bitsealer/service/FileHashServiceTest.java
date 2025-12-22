package com.bitsealer.service;

import com.bitsealer.dto.FileHashDto;
import com.bitsealer.exception.UnauthorizedException;
import com.bitsealer.mapper.FileHashMapper;
import com.bitsealer.model.AppUser;
import com.bitsealer.model.FileHash;
import com.bitsealer.model.FileStamp;
import com.bitsealer.model.StampStatus;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.FileStampRepository;
import com.bitsealer.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FileHashServiceTest {

    @Mock
    private FileHashRepository fileHashRepository;

    @Mock
    private FileStampRepository fileStampRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileHashMapper fileHashMapper;

    @Mock
    private StamperClient stamperClient;

    @InjectMocks
    private FileHashService fileHashService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listMineDtos_returnsHashesForCurrentUser() {
        // given
        String username = "test@example.com";

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername(username);

        given(userRepository.findByUsername(username))
                .willReturn(Optional.of(user));

        FileHash fileHash = new FileHash();
        fileHash.setId(42L);
        fileHash.setFileName("document.pdf");
        fileHash.setSha256("abc123");
        fileHash.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));

        List<FileHash> fileHashes = List.of(fileHash);

        given(fileHashRepository.findByOwnerOrderByCreatedAtDesc(user))
                .willReturn(fileHashes);

        FileStamp stamp = new FileStamp();
        stamp.setFileHash(fileHash);
        stamp.setStatus(StampStatus.SEALED);
        stamp.setSealedAt(LocalDateTime.of(2025, 1, 2, 10, 0));

        // 👇 ESTE es el método real que usa el service
        given(fileStampRepository.findAllByFileHashIn(fileHashes))
                .willReturn(List.of(stamp));

        FileHashDto dto = new FileHashDto(
                fileHash.getId(),
                fileHash.getFileName(),
                fileHash.getSha256(),
                fileHash.getCreatedAt(),
                100L,
                "SEALED",
                stamp.getSealedAt()
        );

        Map<Long, FileStamp> stampMap = Map.of(42L, stamp);

        given(fileHashMapper.toDto(fileHashes, stampMap))
                .willReturn(List.of(dto));

        // when
        List<FileHashDto> result = fileHashService.listMineDtos();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);

        then(userRepository).should().findByUsername(username);
        then(fileHashRepository).should().findByOwnerOrderByCreatedAtDesc(user);
        then(fileStampRepository).should().findAllByFileHashIn(fileHashes);
        then(fileHashMapper).should().toDto(fileHashes, stampMap);
    }

    @Test
    void listMineDtos_throwsUnauthorizedException_whenUserNotFound() {
        // given
        String username = "unknown@example.com";

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(userRepository.findByUsername(username))
                .willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> fileHashService.listMineDtos())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User not found for authenticated principal");
    }
}
