package com.magenta.products.infrastructure.adapter.in.web;

import com.magenta.products.domain.model.Property;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PropertyDetailResponse(
        UUID id,
        String reference,
        String type,
        String status,
        LocationDto location,
        SurfaceDto surface,
        LayoutDto layout,
        String condition,
        Integer buildYear,
        EnergyDto energy,
        Set<String> features,
        Set<String> tags,
        List<OperationDto> operations,
        FinancingDto financing,
        List<MediaDto> media,
        Instant publishedAt) {

    public static PropertyDetailResponse from(Property p) {
        return new PropertyDetailResponse(
                p.id(),
                p.reference(),
                p.type().name(),
                p.status().name(),
                new LocationDto(
                        p.location().street(),
                        p.location().postalCode(),
                        p.location().coordinates().lat(),
                        p.location().coordinates().lng(),
                        p.location().zoneId(),
                        p.location().visibility().name()),
                new SurfaceDto(p.surface().builtSqm(), p.surface().usefulSqm(), p.surface().plotSqm()),
                p.layout() != null ? new LayoutDto(
                        p.layout().rooms(), p.layout().bathrooms(),
                        p.layout().floor(), p.layout().hasElevator()) : null,
                p.condition() != null ? p.condition().name() : null,
                p.buildYear(),
                p.energyRating() != null ? new EnergyDto(
                        p.energyRating().consumptionLetter() != null
                                ? p.energyRating().consumptionLetter().name() : null,
                        p.energyRating().consumptionKwh()) : null,
                p.features(),
                p.tags(),
                p.operations().stream()
                        .map(o -> new OperationDto(o.id(), o.type().name(),
                                o.price().amount(), o.price().currency(), o.negotiable(), o.status().name()))
                        .toList(),
                p.financing() != null ? new FinancingDto(p.financing().has90_5_5()) : null,
                p.media().stream()
                        .map(m -> new MediaDto(m.id(), m.kind().name(), m.storageUri(), m.isCover()))
                        .toList(),
                p.publishedAt());
    }

    public record LocationDto(String street, String postalCode, double lat, double lng,
                               UUID zoneId, String visibility) {}
    public record SurfaceDto(BigDecimal builtSqm, BigDecimal usefulSqm, BigDecimal plotSqm) {}
    public record LayoutDto(Integer rooms, Integer bathrooms, Integer floor, Boolean hasElevator) {}
    public record EnergyDto(String consumptionLetter, Double consumptionKwh) {}
    public record OperationDto(UUID id, String type, BigDecimal price, String currency,
                                boolean negotiable, String status) {}
    public record FinancingDto(boolean has90_5_5) {}
    public record MediaDto(UUID id, String kind, String url, boolean isCover) {}
}
