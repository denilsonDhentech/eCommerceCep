package com.dhentech.ecommercezipcode.api;

import com.dhentech.ecommercezipcode.domain.RouteDetails;
import com.dhentech.ecommercezipcode.domain.usecases.RouteValidationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(RouteController.class)
class RouteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RouteValidationUseCase routeValidationUseCase;

    @Test
    @DisplayName("Should return 200 OK and route details when zip code is valid")
    void shouldReturnOkWhenZipCodeIsValid() throws Exception {
        String validZipCode = "01001000";
        RouteDetails mockRouteDetails = new RouteDetails(
                "01001-000",
                "Praça da Sé",
                "Sé",
                "São Paulo",
                "SP",
                "Available"
        );

        when(routeValidationUseCase.execute(validZipCode)).thenReturn(mockRouteDetails);

        mockMvc.perform(get("/api/v1/deliveries/routes/{zipCode}", validZipCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zipCode").value("01001-000"))
                .andExpect(jsonPath("$.city").value("São Paulo"))
                .andExpect(jsonPath("$.deliveryStatus").value("Available"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when zip code contains letters")
    void shouldReturnBadRequestWhenZipCodeHasLetters() throws Exception {
        String invalidZipCode = "01001ABC";

        mockMvc.perform(get("/api/v1/deliveries/routes/{zipCode}", invalidZipCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when zip code length is not 8")
    void shouldReturnBadRequestWhenZipCodeLengthIsInvalid() throws Exception {
        String invalidZipCode = "12345";

        mockMvc.perform(get("/api/v1/deliveries/routes/{zipCode}", invalidZipCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}