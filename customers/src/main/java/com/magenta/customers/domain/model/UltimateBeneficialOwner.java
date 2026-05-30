package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class UltimateBeneficialOwner {
    String nif;
    String name;
    BigDecimal ownershipPct;
    String nationality;
    boolean pepFlag;
}
