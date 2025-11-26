package com.bitsealer.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para tests de integración que necesiten PostgreSQL.
 *
 * - Levanta un contenedor PostgreSQL 14 aislado.
 * - Inyecta automáticamente la URL/usuario/password en Spring
 *   mediante DynamicPropertySource.
 *
 * Todos los tests de integración deben EXTENDER de esta clase.
 */
@Testcontainers
public abstract class PostgresTestContainer {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:14-alpine")
                    .withDatabaseName("bitsealertest")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        // Si quieres, puedes forzar el driver, pero Spring Boot 3.5 lo detecta solo:
        // registry.add("spring.datasource.driver-class-name", POSTGRES_CONTAINER::getDriverClassName);
    }
}
