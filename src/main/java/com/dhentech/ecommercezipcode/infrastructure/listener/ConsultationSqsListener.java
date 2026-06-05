package com.dhentech.ecommercezipcode.infrastructure.listener;

import com.dhentech.ecommercezipcode.domain.ConsultationEvent;
import com.dhentech.ecommercezipcode.domain.ConsultationLog;
import com.dhentech.ecommercezipcode.infrastructure.repository.ConsultationLogRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsultationSqsListener {

    private static final Logger log = LoggerFactory.getLogger(ConsultationSqsListener.class);
    private final ConsultationLogRepository repository;

    public ConsultationSqsListener(ConsultationLogRepository repository) {
        this.repository = repository;
    }

    @SqsListener("consultation-log-queue")
    public void handleSqsMessage(ConsultationEvent event) {
        log.info("Received message from SQS Queue for Zip Code: {}", event.zipCode());

        ConsultationLog logEntity = new ConsultationLog(
                event.zipCode(),
                event.payloadResponse(),
                event.consultedAt()
        );

        repository.save(logEntity);
        log.info("Log successfully saved to the database via SQS worker.");
    }
}