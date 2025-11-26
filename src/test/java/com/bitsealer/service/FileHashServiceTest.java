package com.bitsealer.service;

import com.bitsealer.dto.FileHashDto;
import com.bitsealer.mapper.FileHashMapper;
import com.bitsealer.model.AppUser;
import com.bitsealer.model.FileHash;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // Importar para capturar la entidad AppUser
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileHashServiceTest {

    @Mock
    private FileHashRepository fileHashRepository;

    @Mock
    private FileHashMapper fileHashMapper;

    @Mock
    private UserRepository userRepository;

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
        
        // 1. Configurar SecurityContext (simulando que el email es el principal)
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()
                );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 2. Crear la entidad AppUser que será devuelta por UserRepository
        AppUser user = new AppUser();
        user.setId(1L);
        // NOTA: Tu servicio busca por username, si el principal es el email, user.setUsername() debe ser el email
        user.setUsername(username); 
        
        // 3. Mockear UserRepository: Cuando se busca por username, devuelve el usuario.
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        // 4. Crear la entidad FileHash
        FileHash entity = new FileHash();
        entity.setId(42L);
        entity.setFileName("document.pdf"); // Corregido a setFileName (lo que tienes en tu entidad)
        entity.setSha256("abc123");
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));

        // 5. Mockear FileHashRepository: Cuando se busca por la instancia de AppUser, devuelve la lista de entidades.
        given(fileHashRepository.findByOwnerOrderByCreatedAtDesc(user)) // 🛑 CORREGIDO: Usamos la instancia 'user'
                .willReturn(List.of(entity));

        // 6. Crear el DTO y mockear el Mapper
        FileHashDto dto = new FileHashDto(
                entity.getId(),
                entity.getFileName(), 
                entity.getSha256(),
                entity.getCreatedAt()
        );
        
        // El mapper recibe la entidad y devuelve el DTO
        given(fileHashMapper.toDto(List.of(entity))).willReturn(List.of(dto)); // 🛑 CORREGIDO: Tu mapper acepta una lista

        // when
        List<FileHashDto> result = fileHashService.listMineDtos();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);

        // Verificar las llamadas
        then(userRepository).should().findByUsername(username);
        then(fileHashRepository).should().findByOwnerOrderByCreatedAtDesc(user); // 🛑 CORREGIDO: Usamos la instancia 'user'
        then(fileHashMapper).should().toDto(List.of(entity));
    }
}