package com.dhentech.ecommercezipcode.api;

import com.dhentech.ecommercezipcode.domain.RouteDetails;
import com.dhentech.ecommercezipcode.domain.usecases.RouteValidationUseCase;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deliveries/routes")
@Validated
public class RouteController {

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);
    private final RouteValidationUseCase routeValidationUseCase;

    public RouteController(RouteValidationUseCase routeValidationUseCase) {
        this.routeValidationUseCase = routeValidationUseCase;
    }

    @GetMapping("/{zipCode}")
    public ResponseEntity<RouteDetails> getRouteDetails(
            @PathVariable
            @Pattern(regexp = "^\\d{8}$", message = "Zip code must contain exactly 8 digits")
            String zipCode) {

        log.info("Received request to validate route for Zip Code: {}", zipCode);

        RouteDetails routeDetails = routeValidationUseCase.execute(zipCode);

        return ResponseEntity.ok(routeDetails);
    }
}