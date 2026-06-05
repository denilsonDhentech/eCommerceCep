package com.dhentech.ecommercezipcode.infrastructure.listener;

import com.dhentech.ecommercezipcode.domain.ConsultationEvent;
import com.dhentech.ecommercezipcode.domain.ConsultationLog;
import com.dhentech.ecommercezipcode.infrastructure.repository.ConsultationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ConsultationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ConsultationEventListener.class);
    private final ConsultationLogRepository repository;

    public ConsultationEventListener(ConsultationLogRepository repository) {
        this.repository = repository;
    }


    @Async
    @EventListener
    public void handleConsultationEvent(ConsultationEvent event) {
        log.info("Async Listener triggered for Zip Code: {}", event.zipCode());

        ConsultationLog consultationLog = new ConsultationLog(
                event.zipCode(),
                event.payloadResponse(),
                event.consultedAt()
        );

        repository.save(consultationLog);

        log.info("Log successfully saved to the database in background.");
    }
}