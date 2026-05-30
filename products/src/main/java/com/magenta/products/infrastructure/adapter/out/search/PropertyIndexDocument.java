package com.magenta.products.infrastructure.adapter.out.search;

import com.magenta.products.domain.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * OpenSearch index document for properties_v1.
 * Plain record — no framework dependencies.
 */
public record PropertyIndexDocument(
        String id,
        String tenantId,
        String type,
        String status,
        ZoneDoc zone,
        GeoPointDoc coordinates,
        BigDecimal price,
        String operationType,
        BigDecimal builtSqm,
        Integer rooms,
        Integer bathrooms,
        String energy,
        Set<String> features,
        Set<String> tags,
        FinancingDoc financing,
        String text,
        Instant publishedAt) {

    public static PropertyIndexDocument from(Property p) {
        Operation activeOp = p.operations().stream()
                .filter(o -> o.status() == OperationStatus.ACTIVE)
                .findFirst().orElse(null);

        return new PropertyIndexDocument(
                p.id().toString(),
                p.tenantId().toString(),
                p.type().name(),
                p.status().name(),
                new ZoneDoc(p.location().zoneId() != null ? p.location().zoneId().toString() : null,
                        List.of()),
                new GeoPointDoc(p.location().coordinates().lat(), p.location().coordinates().lng()),
                activeOp != null ? activeOp.price().amount() : null,
                activeOp != null ? activeOp.type().name() : null,
                p.surface().builtSqm(),
                p.layout() != null ? p.layout().rooms() : null,
                p.layout() != null ? p.layout().bathrooms() : null,
                p.energyRating() != null && p.energyRating().consumptionLetter() != null
                        ? p.energyRating().consumptionLetter().name() : null,
                p.features(),
                p.tags(),
                new FinancingDoc(p.financing() != null
                        ? p.financing().feasibleBankProductIds().stream()
                                .map(UUID::toString).toList() : List.of(),
                        p.financing() != null && p.financing().has90_5_5()),
                buildText(p),
                p.publishedAt());
    }

    private static String buildText(Property p) {
        StringBuilder sb = new StringBuilder(p.reference());
        if (p.subtype() != null) sb.append(" ").append(p.subtype());
        p.tags().forEach(t -> sb.append(" ").append(t));
        return sb.toString();
    }

    public record ZoneDoc(String id, List<String> path) {}
    public record GeoPointDoc(double lat, double lon) {}
    public record FinancingDoc(List<String> feasibleBankProductIds, boolean has90_5_5) {}
}
