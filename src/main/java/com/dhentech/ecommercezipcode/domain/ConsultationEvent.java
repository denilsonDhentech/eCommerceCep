package com.dhentech.ecommercezipcode.domain;

import java.time.LocalDateTime;

public record ConsultationEvent(
        String zipCode,
        String payloadResponse,
        LocalDateTime consultedAt
) {}
