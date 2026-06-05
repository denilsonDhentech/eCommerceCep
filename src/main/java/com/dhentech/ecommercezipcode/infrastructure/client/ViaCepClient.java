package com.dhentech.ecommercezipcode.infrastructure.client;

import com.dhentech.ecommercezipcode.infrastructure.client.dto.ViaCepResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "viaCepClient", url = "${logistics.viacep.url}", fallback = ViaCepClientFallback.class)
public interface ViaCepClient {

    @GetMapping("/ws/{cep}/json/")
    ViaCepResponse consultZipCode(@PathVariable("cep") String cep);
}
