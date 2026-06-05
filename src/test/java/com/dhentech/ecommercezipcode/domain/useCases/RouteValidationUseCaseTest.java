package com.dhentech.ecommercezipcode.domain.useCases;

import com.dhentech.ecommercezipcode.domain.ConsultationEvent;
import com.dhentech.ecommercezipcode.domain.RouteDetails;
import com.dhentech.ecommercezipcode.domain.usecases.RouteValidationUseCase;
import com.dhentech.ecommercezipcode.infrastructure.client.ViaCepClient;
import com.dhentech.ecommercezipcode.infrastructure.client.dto.ViaCepResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteValidationUseCaseTest {

    @Mock
    private ViaCepClient viaCepClient;

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RouteValidationUseCase useCase;

    @Test
    @DisplayName("Should return available route details and send message to SQS on successful API call")
    void shouldReturnAvailableRouteAndPublishEvent() throws JsonProcessingException {
        String zipCode = "01001000";
        ViaCepResponse mockResponse = new ViaCepResponse(
                "01001-000", "Praça da Sé", "lado ímpar", "Sé", "São Paulo", "SP", null
        );

        when(viaCepClient.consultZipCode(zipCode)).thenReturn(mockResponse);
        when(objectMapper.writeValueAsString(mockResponse)).thenReturn("{\"cep\":\"01001-000\"}");

        RouteDetails result = useCase.execute(zipCode);

        assertNotNull(result);
        assertEquals("01001-000", result.zipCode());
        assertEquals("São Paulo", result.city());
        assertEquals("Available", result.deliveryStatus());

        verify(sqsTemplate, times(1)).send(eq("consultation-log-queue"), any(ConsultationEvent.class));
    }

    @Test
    @DisplayName("Should return unavailable route details when API fallback is triggered and send to SQS")
    void shouldReturnUnavailableRouteOnApiFallback() throws JsonProcessingException {
        String zipCode = "01001000";
        ViaCepResponse fallbackResponse = new ViaCepResponse(
                zipCode, "Indisponível", "", "Indisponível", "Indisponível", "Indisponível", "API Externa Offline"
        );

        when(viaCepClient.consultZipCode(zipCode)).thenReturn(fallbackResponse);
        when(objectMapper.writeValueAsString(fallbackResponse)).thenReturn("{}");

        RouteDetails result = useCase.execute(zipCode);

        assertNotNull(result);
        assertEquals("Unavailable - API Externa Offline", result.deliveryStatus());
        assertEquals("Indisponível", result.city());

        ArgumentCaptor<ConsultationEvent> eventCaptor = ArgumentCaptor.forClass(ConsultationEvent.class);
        verify(sqsTemplate).send(eq("consultation-log-queue"), eventCaptor.capture());

        ConsultationEvent publishedEvent = eventCaptor.getValue();
        assertEquals(zipCode, publishedEvent.zipCode());
    }
}