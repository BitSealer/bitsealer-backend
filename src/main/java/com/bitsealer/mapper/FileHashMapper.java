package com.bitsealer.mapper;

import com.bitsealer.dto.FileHashDetailsDto;
import com.bitsealer.dto.FileHashDto;
import com.bitsealer.model.FileHash;
import com.bitsealer.model.FileStamp;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileHashMapper {

    public FileHashDto toDto(FileHash e, FileStamp stamp) {
        return new FileHashDto(
                e.getId(),
                e.getFileName(),
                e.getSha256(),
                e.getCreatedAt(),
                stamp != null ? stamp.getId() : null,
                stamp != null ? stamp.getStatus().name() : null,
                stamp != null ? stamp.getSealedAt() : null
        );
    }

    public FileHashDto toDto(FileHash e) {
        return toDto(e, null);
    }

    public List<FileHashDto> toDto(List<FileHash> list) {
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<FileHashDto> toDto(List<FileHash> list, java.util.Map<Long, FileStamp> stampByFileHashId) {
        return list.stream()
                .map(fh -> toDto(fh, stampByFileHashId.get(fh.getId())))
                .collect(Collectors.toList());
    }

    /**
     * DTO enriquecido para vista detalle.
     * Nota: no devolvemos binarios; el proof se descarga por endpoints blob.
     */
    public FileHashDetailsDto toDetailsDto(FileHash fh, FileStamp stamp) {
        final boolean otsAvailable = stamp != null && stamp.getOtsProof() != null && stamp.getOtsProof().length > 0;
        final int otsBytes = otsAvailable ? stamp.getOtsProof().length : 0;

        return new FileHashDetailsDto(
                fh.getId(),
                fh.getFileName(),
                fh.getSha256(),
                fh.getCreatedAt(),
                fh.getOwner() != null ? fh.getOwner().getUsername() : null,

                stamp != null ? stamp.getId() : null,
                stamp != null ? stamp.getStatus().name() : null,
                stamp != null ? stamp.getCreatedAt() : null,
                stamp != null ? stamp.getLastCheckedAt() : null,
                stamp != null ? stamp.getNextCheckAt() : null,
                stamp != null ? stamp.getAttempts() : null,
                stamp != null ? stamp.getTxid() : null,
                stamp != null ? stamp.getSealedAt() : null,
                stamp != null ? stamp.getLastError() : null,

                otsAvailable,
                otsBytes
        );
    }
}
