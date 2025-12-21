package com.bitsealer.dto;

/**
 * Petición enviada al microservicio "stamper".
 *
 * Para generar un proof .ots válido (compatible con `ots info/verify/upgrade`)
 * el stamper necesita el CONTENIDO del fichero (fileBase64), no solo el hash.
 */
public record StamperStartRequest(
        Long stampId,
        String sha256,
        String originalFilename,
        String fileBase64
) {}
