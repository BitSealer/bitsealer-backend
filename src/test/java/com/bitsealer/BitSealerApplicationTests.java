package com.bitsealer;

import com.bitsealer.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BitSealerApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
        // simple smoke test: arranca el contexto con el mismo Postgres de Testcontainers
    }
}
