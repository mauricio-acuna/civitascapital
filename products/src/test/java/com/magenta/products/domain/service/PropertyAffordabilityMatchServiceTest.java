package com.magenta.products.domain.service;

import com.magenta.products.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyAffordabilityMatchServiceTest {

    private final PropertyAffordabilityMatchService service = new PropertyAffordabilityMatchService();
    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID ZONE = UUID.randomUUID();

    @Test
    void classifiesPropertiesByAffordability() {
        Property viable = property("REF-V", ZONE, 3, "275000");
        Property tight = property("REF-T", ZONE, 3, "310000");
        Property notViable = property("REF-N", ZONE, 3, "380000");

        List<PropertyAffordabilityMatchService.Match> matches = service.match(
                new PropertyAffordabilityMatchService.Query(
                        TENANT, new BigDecimal("285000"), Set.of(ZONE), 3),
                List.of(notViable, tight, viable));

        assertThat(matches).extracting(PropertyAffordabilityMatchService.Match::status)
                .containsExactly(
                        PropertyAffordabilityMatchService.MatchStatus.VIABLE,
                        PropertyAffordabilityMatchService.MatchStatus.TIGHT,
                        PropertyAffordabilityMatchService.MatchStatus.NOT_VIABLE);
    }

    @Test
    void excludesPropertiesOutsideRequestedZones() {
        Property outside = property("REF-OUT", UUID.randomUUID(), 3, "250000");

        List<PropertyAffordabilityMatchService.Match> matches = service.match(
                new PropertyAffordabilityMatchService.Query(
                        TENANT, new BigDecimal("285000"), Set.of(ZONE), 3),
                List.of(outside));

        assertThat(matches).isEmpty();
    }

    private Property property(String reference, UUID zoneId, int rooms, String price) {
        Property property = Property.create(
                UUID.randomUUID(), TENANT, reference, PropertyType.FLAT, null,
                new OwnerInfo("PRIVATE", null, "Owner"),
                new Location("C/ Demo", "1", null, null, "28001",
                        new GeoPoint(40.4, -3.7), zoneId, null,
                        LocationVisibility.NEIGHBORHOOD_ONLY),
                new Surface(new BigDecimal("90"), null, null),
                new Layout(rooms, 2, null, null, null, 1, true),
                PropertyCondition.GOOD, 2005,
                new EnergyRating(EnergyLetter.C, 120.0, EnergyLetter.C, 20.0, "CERT", null),
                Set.of("fiber"), Set.of(), Set.of(), "agent");

        property.addOperation(new Operation(
                UUID.randomUUID(), property.id(), OperationType.SALE,
                Money.euros(new BigDecimal(price)),
                null, null, null, null, true,
                null, null, OperationStatus.ACTIVE, false, null));
        return property;
    }
}

