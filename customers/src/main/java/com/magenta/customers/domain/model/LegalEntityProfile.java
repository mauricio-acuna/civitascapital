package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@With
public class LegalEntityProfile {

    private final String cif;
    private final String legalName;
    private final String tradeName;
    private final String legalForm;           // SL, SA, SCP, SLU, SOCIMI, …
    private final String regMercantilNumber;
    private final LocalDate foundedAt;
    private final String cnae;
    private final String representativeNif;
    private final PostalAddress address;
    private final List<UltimateBeneficialOwner> uboList;
}
