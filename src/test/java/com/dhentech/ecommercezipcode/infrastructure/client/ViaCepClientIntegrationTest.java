package com.dhentech.ecommercezipcode.infrastructure.client;

import com.dhentech.ecommercezipcode.infrastructure.client.dto.ViaCepResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "logistics.viacep.url=http://localhost:${wiremock.server.port}",
        "spring.cloud.openfeign.circuitbreaker.enabled=true"
})
class ViaCepClientIntegrationTest {

    @Autowired
    private ViaCepClient viaCepClient;

    @Test
    @DisplayName("Deve retornar os dados do CEP com sucesso quando a API externa responder 200 OK")
    void mustReturnZipCodeDataSuccessfully() {
        String zipCode = "01001000";
        String jsonResponse = """
            {
              "cep": "01001-000",
              "logradouro": "Praça da Sé",
              "complemento": "lado ímpar",
              "bairro": "Sé",
              "localidade": "São Paulo",
              "uf": "SP"
            }
            """;

        stubFor(get(urlEqualTo("/ws/" + zipCode + "/json/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        ViaCepResponse response = viaCepClient.consultZipCode(zipCode);

        assertNotNull(response);
        assertEquals("01001-000", response.cep());
        assertEquals("Praça da Sé", response.logradouro());
        assertEquals("São Paulo", response.localidade());
        assertNull(response.erro());
    }

    @Test
    @DisplayName("Deve acionar o Fallback quando a API externa retornar erro (ex: 500 Server Error)")
    void shouldTriggerFallbackWhenAPIFails() {
        String zipCode = "01001000";

        stubFor(get(urlEqualTo("/ws/" + zipCode + "/json/"))
                .willReturn(aResponse()
                        .withStatus(500)));

        ViaCepResponse response = viaCepClient.consultZipCode(zipCode);

        assertNotNull(response);
        assertEquals("API Externa Offline", response.erro());
        assertEquals("Indisponível", response.localidade());
    }

    @Test
    @DisplayName("Deve retornar status de falha quando a API demorar muito para responder (Timeout)")
    void shouldHandleTimeoutGracefully() {
        String zipCode = "01001000";

        stubFor(get(urlEqualTo("/ws/" + zipCode + "/json/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(5000) // Simula 5 segundos de espera
                        .withBody("{\"cep\": \"01001-000\"}")));

        ViaCepResponse response = viaCepClient.consultZipCode(zipCode);

        assertNotNull(response);
        assertEquals("API Externa Offline", response.erro());
    }

    @Test
    @DisplayName("Deve identificar erro de negócio quando o CEP não existe na base da ViaCep")
    void shouldHandleZipCodeNotFound() {
        String zipCode = "00000000";

        stubFor(get(urlEqualTo("/ws/" + zipCode + "/json/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"erro\": \"true\"}")));

        ViaCepResponse response = viaCepClient.consultZipCode(zipCode);

        assertNotNull(response);
        assertEquals("true", response.erro());
    }
}