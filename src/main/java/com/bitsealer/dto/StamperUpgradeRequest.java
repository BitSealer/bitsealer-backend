package com.bitsealer.dto;

/**
 * Petición para /upgrade del stamper.
 * Enviamos el proof actual en Base64; el stamper intenta `ots upgrade` y devuelve el proof actualizado.
 */
public record StamperUpgradeRequest(
        Long stampId,
        String otsProofB64
) {}
