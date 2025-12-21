package com.bitsealer.dto;

/**
 * Respuesta de /upgrade del stamper.
 */
public record StamperUpgradeResponse(
        Long stampId,
        String status,
        String otsProofB64,
        String txid
) {}
