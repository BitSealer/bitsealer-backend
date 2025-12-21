package com.bitsealer.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils buildJwtUtils() {
        JwtUtils jwtUtils = new JwtUtils();
        // Clave de prueba: respeta el mínimo de 32 caracteres (bytes)
        String testSecret = "una_clave_de_32_caracteres_minimo_123456";
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testSecret);
        // Validamos inicialización (simulando PostConstruct)
        ReflectionTestUtils.invokeMethod(jwtUtils, "init");
        return jwtUtils;
    }

    @Test
    @DisplayName("Fail fast: lanza excepción si la secret es demasiado corta")
    void init_ShortSecret_ThrowsException() {
        JwtUtils jwtUtils = new JwtUtils();
        // Secret corta
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "short_secret");

        try {
            ReflectionTestUtils.invokeMethod(jwtUtils, "init");
            // Si no falla, forzamos error en test
            org.junit.jupiter.api.Assertions.fail("Debió lanzar IllegalStateException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("debe tener al menos 32 bytes");
        }
    }

    @Test
    @DisplayName("generateAccessToken y getSubjectFromToken funcionan con un token de acceso válido")
    void generateAndParseAccessToken() {
        JwtUtils jwtUtils = buildJwtUtils();

        String email = "user@example.com";
        String token = jwtUtils.generateAccessToken(email);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getSubjectFromToken(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("generateRefreshToken y getSubjectFromToken funcionan con un token de refresco válido")
    void generateAndParseRefreshToken() {
        JwtUtils jwtUtils = buildJwtUtils();

        String email = "user@example.com";
        String token = jwtUtils.generateRefreshToken(email);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getSubjectFromToken(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("validateToken devuelve false si el token está manipulado")
    void validateToken_ManipulatedToken_ReturnsFalse() {
        JwtUtils jwtUtils = buildJwtUtils();

        String email = "user@example.com";
        String token = jwtUtils.generateAccessToken(email);
        String manipulated = token + "x";

        assertThat(jwtUtils.validateToken(manipulated)).isFalse();
    }
}
