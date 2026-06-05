package com.dhentech.ecommercezipcode.domain;

public record RouteDetails(
        String zipCode,
        String street,
        String neighborhood,
        String city,
        String state,
        String deliveryStatus
) {}
