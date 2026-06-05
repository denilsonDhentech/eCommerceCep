package com.dhentech.ecommercezipcode.infrastructure.client;

import com.dhentech.ecommercezipcode.infrastructure.client.dto.ViaCepResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ViaCepClientFallbackTest {

    private final ViaCepClientFallback fallback = new ViaCepClientFallback();

    @Test
    @DisplayName("Deve retornar um DTO com dados de indisponibilidade quando o fallback for acionado")
    void mustReturnDateOfUnavailability() {
        String testingZipCode = "01001000";

        ViaCepResponse response = fallback.consultZipCode(testingZipCode);

        assertNotNull(response);
        assertEquals(testingZipCode, response.cep());
        assertEquals("Indisponível", response.logradouro());
        assertEquals("API Externa Offline", response.erro());
    }
}
