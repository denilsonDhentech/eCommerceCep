package com.dhentech.ecommercezipcode.infrastructure.client;

import com.dhentech.ecommercezipcode.infrastructure.client.dto.ViaCepResponse;
import org.springframework.stereotype.Component;

@Component
public class ViaCepClientFallback implements ViaCepClient {

    public ViaCepResponse consultZipCode(String cep) {
        return new ViaCepResponse(
                cep,
                "Indisponível",
                "",
                "Indisponível",
                "Indisponível",
                "Indisponível",
                "API Externa Offline"
        );
    }
}
