package com.bitsealer.dto;

import com.bitsealer.model.FileHash;
import java.time.LocalDateTime;

public record FileHashDto(
        Long id,
        String originalFilename,
        String sha256,
        LocalDateTime createdAt,

        // NUEVO
        Long stampId,
        String stampStatus,
        LocalDateTime sealedAt
) {
    public FileHashDto(FileHash entity) {
        this(
                entity.getId(),
                entity.getFileName(),
                entity.getSha256(),
                entity.getCreatedAt(),
                null,
                null,
                null
        );
    }
}
