package com.dhentech.ecommercezipcode.domain.usecases;

import com.dhentech.ecommercezipcode.domain.ConsultationEvent;
import com.dhentech.ecommercezipcode.domain.RouteDetails;
import com.dhentech.ecommercezipcode.infrastructure.client.ViaCepClient;
import com.dhentech.ecommercezipcode.infrastructure.client.dto.ViaCepResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RouteValidationUseCase {

    private final ViaCepClient viaCepClient;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public RouteValidationUseCase(ViaCepClient viaCepClient,
                                  ApplicationEventPublisher eventPublisher,
                                  ObjectMapper objectMapper) {
        this.viaCepClient = viaCepClient;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    public RouteDetails execute(String zipCode) {
        ViaCepResponse response = viaCepClient.consultZipCode(zipCode);

        String deliveryStatus = "Available";
        if (response.erro() != null) {
            deliveryStatus = "Unavailable - " + response.erro();
        }

        RouteDetails routeDetails = new RouteDetails(
                response.cep(),
                response.logradouro(),
                response.bairro(),
                response.localidade(),
                response.uf(),
                deliveryStatus
        );

        publishLogEvent(zipCode, response);

        return routeDetails;
    }

    private void publishLogEvent(String zipCode, ViaCepResponse response) {
        try {
            String payload = objectMapper.writeValueAsString(response);
            ConsultationEvent event = new ConsultationEvent(zipCode, payload, LocalDateTime.now());

            eventPublisher.publishEvent(event);

        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize response payload for logging.");
        }
    }
}
