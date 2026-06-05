package com.dhentech.ecommercezipcode.domain.useCases;

import com.dhentech.ecommercezipcode.domain.ConsultationEvent;
import com.dhentech.ecommercezipcode.domain.RouteDetails;
import com.dhentech.ecommercezipcode.domain.usecases.RouteValidationUseCase;
import com.dhentech.ecommercezipcode.infrastructure.client.ViaCepClient;
import com.dhentech.ecommercezipcode.infrastructure.client.dto.ViaCepResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteValidationUseCaseTest {

    @Mock
    private ViaCepClient viaCepClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RouteValidationUseCase useCase;

    @Test
    @DisplayName("Should return available route details and publish event on successful API call")
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

        verify(eventPublisher, times(1)).publishEvent(any(ConsultationEvent.class));
    }

    @Test
    @DisplayName("Should return unavailable route details when API fallback is triggered")
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
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ConsultationEvent publishedEvent = eventCaptor.getValue();
        assertEquals(zipCode, publishedEvent.zipCode());
    }
}
