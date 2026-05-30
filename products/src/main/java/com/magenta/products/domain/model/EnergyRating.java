package com.magenta.products.domain.model;

import java.time.LocalDate;

public record EnergyRating(
        EnergyLetter consumptionLetter,
        Double consumptionKwh,
        EnergyLetter emissionsLetter,
        Double emissionsKgCo2,
        String certificateNumber,
        LocalDate validUntil) {
}
