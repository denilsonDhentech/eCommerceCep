package com.dhentech.ecommercezipcode.domain.usecases;

import com.dhentech.ecommercezipcode.domain.ConsultationEvent;
import com.dhentech.ecommercezipcode.domain.RouteDetails;
import com.dhentech.ecommercezipcode.infrastructure.client.ViaCepClient;
import com.dhentech.ecommercezipcode.infrastructure.client.dto.ViaCepResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RouteValidationUseCase {

    private final ViaCepClient viaCepClient;
    private final ObjectMapper objectMapper;
    private final SqsTemplate sqsTemplate;

    public RouteValidationUseCase(ViaCepClient viaCepClient,
                                  ObjectMapper objectMapper,
                                  SqsTemplate sqsTemplate) {
        this.viaCepClient = viaCepClient;
        this.objectMapper = objectMapper;
        this.sqsTemplate = sqsTemplate;
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

            sqsTemplate.send("consultation-log-queue", event);

        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize response payload for logging.");
        }
    }
}