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
import java.util.List;

@Service
public class FileHashService {

    private final FileHashRepository hashRepo;
    private final UserRepository     userRepo;
    private final FileHashMapper     mapper;      // ← nuevo

    public FileHashService(FileHashRepository hashRepo,
                           UserRepository userRepo,
                           FileHashMapper mapper) {
        this.hashRepo = hashRepo;
        this.userRepo = userRepo;
        this.mapper   = mapper;
    }

    /* --------------------------------------------------------- */
    /* 1) Métodos que devuelven DTOs para la vista                */
    /* --------------------------------------------------------- */
    public List<FileHashDto> findDtosByUser(AppUser owner) {
        return mapper.toDto(hashRepo.findByOwnerOrderByCreatedAtDesc(owner));
    }

    public List<FileHashDto> listMineDtos() {
        return mapper.toDto(hashRepo.findByOwnerOrderByCreatedAtDesc(getCurrentUser()));
    }

    /* --------------------------------------------------------- */
    /* 2) Guardar desde archivo                                  */
    /* --------------------------------------------------------- */
    public FileHash saveForUser(AppUser owner, MultipartFile file) throws IOException {
        String sha256 = DigestUtils.sha256Hex(file.getInputStream());
        return saveForUser(owner, sha256, file.getOriginalFilename());
    }

    public FileHash saveForUser(AppUser owner, String sha256, String fileName) {
        FileHash fh = new FileHash();
        fh.setOwner(owner);
        fh.setSha256(sha256);
        fh.setFileName(fileName);
        return hashRepo.save(fh);
    }

    /* --------------------------------------------------------- */
    /* 3) Helper: usuario autenticado                            */
    /* --------------------------------------------------------- */
    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                                               .getAuthentication()
                                               .getName();
        return userRepo.findByUsername(username).orElseThrow();
    }
}
