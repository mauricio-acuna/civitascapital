package com.magenta.products.domain.service;

import com.magenta.products.domain.model.Operation;
import com.magenta.products.domain.model.OperationStatus;
import com.magenta.products.domain.model.OperationType;
import com.magenta.products.domain.model.Property;
import com.magenta.products.domain.model.PropertyStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Motor puro de matching de inmuebles contra capacidad de compra.
 *
 * Pensado para Civitas Pro: no busca "todos los pisos", prioriza inmuebles que
 * el comprador puede comprar o que quedan cerca de su rango financiero.
 */
public class PropertyAffordabilityMatchService {

    private static final BigDecimal TIGHT_THRESHOLD = BigDecimal.valueOf(1.12);

    public List<Match> match(Query query, List<Property> candidates) {
        if (query == null) throw new IllegalArgumentException("query is required");
        if (query.maxTicket() == null || query.maxTicket().signum() <= 0) {
            throw new IllegalArgumentException("maxTicket must be positive");
        }
        if (candidates == null || candidates.isEmpty()) return List.of();

        return candidates.stream()
                .map(property -> classify(query, property))
                .filter(match -> match.status() != MatchStatus.OUT_OF_SCOPE)
                .sorted(Comparator
                        .comparing(Match::status)
                        .thenComparing(Match::priceGapAbs)
                        .thenComparing(match -> match.property().reference()))
                .toList();
    }

    private Match classify(Query query, Property property) {
        Operation operation = activeSaleOperation(property);
        if (operation == null || operation.price() == null) {
            return Match.outOfScope(property);
        }
        if (property.status() == PropertyStatus.ARCHIVED) {
            return Match.outOfScope(property);
        }
        if (query.zoneIds() != null && !query.zoneIds().isEmpty()
                && !query.zoneIds().contains(property.location().zoneId())) {
            return Match.outOfScope(property);
        }
        if (query.roomsMin() != null && property.layout() != null && property.layout().rooms() != null
                && property.layout().rooms() < query.roomsMin()) {
            return Match.outOfScope(property);
        }

        BigDecimal price = operation.price().amount();
        BigDecimal gap = price.subtract(query.maxTicket()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ratio = price.divide(query.maxTicket(), 4, RoundingMode.HALF_UP);
        MatchStatus status;
        if (price.compareTo(query.maxTicket()) <= 0) {
            status = MatchStatus.VIABLE;
        } else if (price.compareTo(query.maxTicket().multiply(TIGHT_THRESHOLD)) <= 0) {
            status = MatchStatus.TIGHT;
        } else {
            status = MatchStatus.NOT_VIABLE;
        }

        return new Match(property, operation, status, gap, ratio);
    }

    private Operation activeSaleOperation(Property property) {
        if (property == null) return null;
        return property.operations().stream()
                .filter(operation -> operation.type() == OperationType.SALE)
                .filter(operation -> operation.status() == OperationStatus.ACTIVE
                        || operation.status() == OperationStatus.DRAFT)
                .findFirst()
                .orElse(null);
    }

    public record Query(
            UUID tenantId,
            BigDecimal maxTicket,
            Set<UUID> zoneIds,
            Integer roomsMin
    ) {}

    public record Match(
            Property property,
            Operation operation,
            MatchStatus status,
            BigDecimal priceGap,
            BigDecimal priceToBudgetRatio
    ) {
        static Match outOfScope(Property property) {
            return new Match(property, null, MatchStatus.OUT_OF_SCOPE, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal priceGapAbs() {
            return priceGap == null ? BigDecimal.ZERO : priceGap.abs();
        }
    }

    public enum MatchStatus {
        VIABLE,
        TIGHT,
        NOT_VIABLE,
        OUT_OF_SCOPE
    }
}

