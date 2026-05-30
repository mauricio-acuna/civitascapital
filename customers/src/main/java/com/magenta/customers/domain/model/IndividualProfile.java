package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@With
public class IndividualProfile {

    // NIF almacenado cifrado; en dominio se trabaja en claro (sólo en memoria)
    private final String nif;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final String nationality;          // ISO 3166-1 alpha-2
    private final String residenceCountry;
    private final String taxResidence;
    private final String civilStatus;
    private final String phone;
    private final String email;
    private final PostalAddress address;
    private final UUID zoneId;
    private final ProfessionalProfile professional;
}
