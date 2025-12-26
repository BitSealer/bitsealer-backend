package com.bitsealer.service;

import com.bitsealer.exception.UnauthorizedException;
import com.bitsealer.model.AppUser;
import com.bitsealer.model.FileHash;
import com.bitsealer.model.FileStamp;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.FileStampRepository;
import com.bitsealer.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

/**
 * Serves downloadable artifacts for a user's file:
 *  - .ots proof
 *  - PDF certificate
 */
@Service
public class FileArtifactService {

    private final FileHashRepository fileHashRepository;
    private final FileStampRepository fileStampRepository;
    private final UserRepository userRepository;
    private final CertificatePdfService certificatePdfService;

    public FileArtifactService(FileHashRepository fileHashRepository,
                               FileStampRepository fileStampRepository,
                               UserRepository userRepository,
                               CertificatePdfService certificatePdfService) {
        this.fileHashRepository = fileHashRepository;
        this.fileStampRepository = fileStampRepository;
        this.userRepository = userRepository;
        this.certificatePdfService = certificatePdfService;
    }

    public DownloadResult downloadOts(Long fileHashId, String username) {
        OwnedPair pair = requireOwned(fileHashId, username);
        FileStamp stamp = pair.stamp;

        if (stamp.getOtsProof() == null || stamp.getOtsProof().length == 0) {
            throw new ResponseStatusException(CONFLICT, "Este archivo aún no tiene .ots generado");
        }

        String base = safeBaseName(pair.fileHash.getFileName(), "proof-" + pair.fileHash.getId());
        return new DownloadResult(stamp.getOtsProof(), base + ".ots", "application/octet-stream");
    }

    public DownloadResult downloadCertificatePdf(Long fileHashId, String username) {
        OwnedPair pair = requireOwned(fileHashId, username);
        FileStamp stamp = pair.stamp;

        if (stamp.getOtsProof() == null || stamp.getOtsProof().length == 0) {
            throw new ResponseStatusException(CONFLICT, "Este archivo aún no tiene .ots generado");
        }

        String otsSha = DigestUtils.sha256Hex(stamp.getOtsProof());
        byte[] pdf = certificatePdfService.generate(pair.fileHash, stamp, otsSha);

        String base = safeBaseName(pair.fileHash.getFileName(), "certificate-" + pair.fileHash.getId());
        return new DownloadResult(pdf, base + "-certificate.pdf", "application/pdf");
    }

    private OwnedPair requireOwned(Long fileHashId, String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found for authenticated principal"));

        FileHash fileHash = fileHashRepository.findById(fileHashId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Archivo no encontrado"));

        if (fileHash.getOwner() == null || user.getId() == null || !user.getId().equals(fileHash.getOwner().getId())) {
            throw new UnauthorizedException("No autorizado para acceder a este archivo");
        }

        FileStamp stamp = fileStampRepository.findByFileHash_Id(fileHashId)
                .orElseThrow(() -> new ResponseStatusException(CONFLICT, "No existe sello asociado"));

        return new OwnedPair(fileHash, stamp);
    }

    private static String safeBaseName(String originalName, String fallback) {
        String name = (originalName == null || originalName.isBlank()) ? fallback : originalName;
        name = name.replaceAll("[\\\\/\\r\\n\\t]", "_").trim();
        name = name.replaceAll("(?i)\\.(ots|pdf)$", "");
        return name;
    }

    private record OwnedPair(FileHash fileHash, FileStamp stamp) {}

    public record DownloadResult(byte[] bytes, String filename, String contentType) {}
}
