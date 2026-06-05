package com.dhentech.ecommercezipcode.infrastructure.repository;

import com.dhentech.ecommercezipcode.domain.ConsultationLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class ConsultationLogRepositoryTest {

    @Autowired
    private ConsultationLogRepository repository;

    @Test
    @DisplayName("Should save a consultation log successfully")
    void shouldSaveLogSuccessfully() {
        ConsultationLog logEntry = new ConsultationLog(
                "01001000",
                "{\"cep\":\"01001-000\"}",
                LocalDateTime.now()
        );

        ConsultationLog savedLog = repository.save(logEntry);

        assertNotNull(savedLog.getId());
        assertTrue(savedLog.getId() > 0);
    }
}
