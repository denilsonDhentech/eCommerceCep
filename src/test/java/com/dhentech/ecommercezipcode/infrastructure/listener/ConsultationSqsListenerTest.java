package com.dhentech.ecommercezipcode.infrastructure.listener;

import com.dhentech.ecommercezipcode.domain.ConsultationEvent;
import com.dhentech.ecommercezipcode.domain.ConsultationLog;
import com.dhentech.ecommercezipcode.infrastructure.repository.ConsultationLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsultationSqsListenerTest {

    @Mock
    private ConsultationLogRepository repository;

    @InjectMocks
    private ConsultationSqsListener listener;

    @Test
    @DisplayName("Should consume SQS message and save the consultation log to the database")
    void shouldHandleSqsMessageAndSaveLog() {
        String zipCode = "01001000";
        String payload = "{\"cep\":\"01001-000\",\"logradouro\":\"Praça da Sé\",\"localidade\":\"São Paulo\"}";
        LocalDateTime consultedAt = LocalDateTime.now();

        ConsultationEvent event = new ConsultationEvent(zipCode, payload, consultedAt);

        listener.handleSqsMessage(event);

        ArgumentCaptor<ConsultationLog> logCaptor = ArgumentCaptor.forClass(ConsultationLog.class);

        verify(repository, times(1)).save(logCaptor.capture());

        ConsultationLog savedLog = logCaptor.getValue();

        assertNotNull(savedLog);
        assertEquals(zipCode, savedLog.getZipcodeQueryed());
        assertEquals(payload, savedLog.getPayloadReturn());
        assertEquals(consultedAt, savedLog.getDateTimeConsultation());
    }
}