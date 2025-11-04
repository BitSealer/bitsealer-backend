package com.bitsealer.service;

import com.bitsealer.dto.FileHashDto;
import com.bitsealer.mapper.FileHashMapper;
import com.bitsealer.model.AppUser;
import com.bitsealer.model.FileHash;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileHashService {

    private final FileHashRepository hashRepo;
    private final UserRepository userRepo;
    private final FileHashMapper mapper;

    public FileHashService(FileHashRepository hashRepo,
                            UserRepository userRepo,
                            FileHashMapper mapper) {
        this.hashRepo = hashRepo;
        this.userRepo = userRepo;
        this.mapper   = mapper;
    }

    /**
     * Guarda un archivo para el usuario actual (tomado del SecurityContext).
     */
    public FileHashDto saveForCurrentUser(MultipartFile file) throws IOException {
        AppUser owner = getCurrentUser();
        // Calcular SHA-256 del archivo
        String sha256 = DigestUtils.sha256Hex(file.getInputStream());
        // Crear entidad y guardar
        FileHash fh = new FileHash();
        fh.setSha256(sha256);
        fh.setFileName(file.getOriginalFilename());
        fh.setOwner(owner);
        fh.setCreatedAt(LocalDateTime.now());
        FileHash saved = hashRepo.save(fh);
        return mapper.toDto(saved);
    }

    /**
     * Lista los archivos del usuario actual, en orden descendente por fecha.
     */
    public List<FileHashDto> listMineDtos() {
        AppUser current = getCurrentUser();
        List<FileHash> files = hashRepo.findByOwnerOrderByCreatedAtDesc(current);
        return mapper.toDto(files);
    }

    // Helper para obtener el usuario autenticado actual desde SecurityContext
    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElseThrow();
    }
}
