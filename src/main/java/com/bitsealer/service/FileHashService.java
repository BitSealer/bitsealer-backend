package com.bitsealer.service;

import com.bitsealer.dto.FileHashDto;
import com.bitsealer.dto.StamperStampResponse;
import com.bitsealer.exception.UnauthorizedException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileHashService {

    private final FileHashRepository fileHashRepository;
    private final FileStampRepository fileStampRepository;
    private final UserRepository userRepository;
    private final FileHashMapper mapper;
    private final StamperClient stamperClient;

    public FileHashService(FileHashRepository fileHashRepository,
                           FileStampRepository fileStampRepository,
                           UserRepository userRepository,
                           FileHashMapper mapper,
                           StamperClient stamperClient) {
        this.fileHashRepository = fileHashRepository;
        this.fileStampRepository = fileStampRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.stamperClient = stamperClient;
    }

    public FileHashDto saveForCurrentUser(MultipartFile file) throws IOException {
        AppUser owner = getCurrentUser();

        byte[] fileBytes = file.getBytes();
        String sha256 = DigestUtils.sha256Hex(fileBytes);

        FileHash fileHash = new FileHash();
        fileHash.setOwner(owner);
        fileHash.setFileName(file.getOriginalFilename());
        fileHash.setSha256(sha256);

        FileHash saved = fileHashRepository.save(fileHash);

        FileStamp stamp = new FileStamp();
        stamp.setFileHash(saved);
        stamp.setStatus(StampStatus.PENDING);
        stamp.setNextCheckAt(LocalDateTime.now());

        FileStamp savedStamp = fileStampRepository.save(stamp);

        try {
            StamperStampResponse resp = stamperClient.stamp(savedStamp.getId(), sha256, file.getOriginalFilename(), fileBytes);

            byte[] otsBytes = Base64.getDecoder().decode(resp.otsProofB64());
            savedStamp.setOtsProof(otsBytes);

            if (resp.txid() != null && !resp.txid().isBlank()) {
                savedStamp.setTxid(resp.txid());
                savedStamp.setStatus(StampStatus.ANCHORING);
            } else {
                savedStamp.setStatus(StampStatus.PENDING);
            }

            savedStamp.setLastError(null);
            fileStampRepository.save(savedStamp);

            return mapper.toDto(saved, savedStamp);

        } catch (Exception e) {
            savedStamp.setStatus(StampStatus.ERROR);
            savedStamp.setLastError("Fallo al generar ots_proof en /stamp: " + e.getMessage());
            //si el stamper esta caido reintenta en 5min
            savedStamp.setNextCheckAt(LocalDateTime.now().plusMinutes(5));
            fileStampRepository.save(savedStamp);
            throw e;
        }
    }

    public List<FileHashDto> listMineDtos() {
        AppUser user = getCurrentUser();

        List<FileHash> fileHashes = fileHashRepository.findByOwnerOrderByCreatedAtDesc(user);
        if (fileHashes.isEmpty()) return List.of();

        List<FileStamp> stamps = fileStampRepository.findAllByFileHashIn(fileHashes);

        Map<Long, FileStamp> stampByFileHashId = stamps.stream()
                .collect(Collectors.toMap(s -> s.getFileHash().getId(), s -> s, (a, b) -> a));

        return mapper.toDto(fileHashes, stampByFileHashId);
    }

    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found for authenticated principal"));
    }
}
