package com.bitsealer.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    // 15 min de validez para access token
    private final long jwtExpirationMs = 15 * 60 * 1000;
    // 7 días para refresh token
    private final long refreshExpirationMs = 7 * 24 * 60 * 60 * 1000;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String subject) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + refreshExpirationMs);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Extrae todos los claims del token. */
    public Claims parseTokenClaims(String token) {
        return Jwts.parser()                    // ✅ nuevo flujo JJWT 0.12.x
                .verifyWith(getSigningKey())    // clave de verificación
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getSubjectFromToken(String token) {
        return parseTokenClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseTokenClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}




