package com.bitsealer.dto;

public record StamperStartRequest(
        Long stampId,
        String sha256
) {}
