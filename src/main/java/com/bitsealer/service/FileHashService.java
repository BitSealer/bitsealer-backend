package com.bitsealer.service;

import com.bitsealer.dto.FileHashDto;
import com.bitsealer.mapper.FileHashMapper;
import com.bitsealer.model.AppUser;
import com.bitsealer.model.FileHash;
import com.bitsealer.model.FileStamp;
import com.bitsealer.model.StampStatus;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.FileStampRepository;
import com.bitsealer.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileHashService {

    private final FileHashRepository hashRepo;
    private final FileStampRepository stampRepo;
    private final UserRepository userRepo;
    private final FileHashMapper mapper;
    private final StamperClient stamperClient;

    public FileHashService(FileHashRepository hashRepo,
                           FileStampRepository stampRepo,
                           UserRepository userRepo,
                           FileHashMapper mapper,
                           StamperClient stamperClient) {
        this.hashRepo = hashRepo;
        this.stampRepo = stampRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
        this.stamperClient = stamperClient;
    }

    /**
     * Guarda hash y crea sello PENDING + dispara el microservicio.
     */
    @Transactional
    public FileHashDto saveForCurrentUser(MultipartFile file) throws IOException {
        AppUser owner = getCurrentUser();

        String sha256 = DigestUtils.sha256Hex(file.getInputStream());

        FileHash fh = new FileHash();
        fh.setSha256(sha256);
        fh.setFileName(file.getOriginalFilename());
        fh.setOwner(owner);
        fh.setCreatedAt(LocalDateTime.now());

        FileHash savedHash = hashRepo.save(fh);

        // Crear sello PENDING
        FileStamp stamp = new FileStamp();
        stamp.setFileHash(savedHash);
        stamp.setStatus(StampStatus.PENDING);
        stamp.setCreatedAt(LocalDateTime.now());
        FileStamp savedStamp = stampRepo.save(stamp);

        // Disparar microservicio (si falla => marcamos ERROR)
        try {
            stamperClient.startStamp(savedStamp.getId(), savedHash.getSha256());
        } catch (Exception ex) {
            savedStamp.setStatus(StampStatus.ERROR);
            savedStamp.setSealedAt(LocalDateTime.now());
            stampRepo.save(savedStamp);
            throw ex;
        }

        return mapper.toDto(savedHash, savedStamp);
    }

    /**
     * Lista historial con estado del sello.
     */
    public List<FileHashDto> listMineDtos() {
        AppUser current = getCurrentUser();

        List<FileHash> files = hashRepo.findByOwnerOrderByCreatedAtDesc(current);
        if (files.isEmpty()) return List.of();

        List<Long> ids = files.stream().map(FileHash::getId).toList();
        List<FileStamp> stamps = stampRepo.findByFileHash_IdIn(ids);

        Map<Long, FileStamp> stampByFileHashId = stamps.stream()
                .collect(Collectors.toMap(s -> s.getFileHash().getId(), s -> s));

        return mapper.toDto(files, stampByFileHashId);
    }

    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new com.bitsealer.exception.UnauthorizedException(
                        "User not found for authenticated principal"));
    }
}
