package com.bitsealer.dto;

import java.time.LocalDateTime;

public record FileHashDto(
        Long id,
        String originalFilename,
        String sha256,
        LocalDateTime createdAt
) {}
