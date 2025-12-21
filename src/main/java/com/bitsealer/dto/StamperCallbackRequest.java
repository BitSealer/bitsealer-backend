package com.bitsealer.dto;

public record StamperCallbackRequest(
        Long stampId,
        String status,       // SEALED / ERROR
        String otsProofB64,  // nullable si ERROR
        String errorMessage  // nullable
) {}
