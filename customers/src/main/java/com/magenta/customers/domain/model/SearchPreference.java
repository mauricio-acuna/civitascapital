package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@With
public class SearchPreference {

    private final UUID id;
    private final UUID customerId;
    private final OperationType operationType;
    private final Set<String> propertyTypes;
    private final BigDecimal priceMin;
    private final BigDecimal priceMax;
    private final Integer surfaceMin;
    private final Short roomsMin;
    private final Short bathroomsMin;
    private final Set<UUID> zoneIds;
    private final boolean requiresFiber;
    private final Integer maxRiskOccupation;
    private final AlertChannel alertChannel;
    private final AlertFrequency alertFrequency;
    private final boolean active;
    private final Instant createdAt;
}
