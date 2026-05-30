package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class HouseholdMember {
    UUID individualId;      // referencia a Customer de tipo INDIVIDUAL
    HouseholdRole role;
    BigDecimal ownershipPct;
}
