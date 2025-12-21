package com.bitsealer.dto;

/**
 * Respuesta del microservicio stamper (stateless).
 *
 * /stamp devuelve el proof .ots en Base64 para que el backend lo persista.
 */
public record StamperStampResponse(
        Long stampId,
        String status,
        String otsProofB64,
        String txid
) {}
