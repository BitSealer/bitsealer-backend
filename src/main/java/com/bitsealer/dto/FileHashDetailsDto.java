package com.bitsealer.dto;

import java.time.LocalDateTime;

/**
 * DTO enriquecido para la vista detalle de un hash.
 *
 * - Pensado para UI (historial → detalle)
 * - No incluye binarios (el .ots se descarga por endpoint blob)
 */
public record FileHashDetailsDto(
        Long id,
        String originalFilename,
        String sha256,
        LocalDateTime createdAt,

        // Info de usuario (útil para auditoría UI)
        String ownerUsername,

        // Info de sellado (FileStamp)
        Long stampId,
        String stampStatus,
        LocalDateTime stampCreatedAt,
        LocalDateTime lastCheckedAt,
        LocalDateTime nextCheckAt,
        Integer attempts,
        String txid,
        LocalDateTime sealedAt,
        String lastError,

        // Metadatos (sin binario)
        Boolean otsAvailable,
        Integer otsBytes
) {}
