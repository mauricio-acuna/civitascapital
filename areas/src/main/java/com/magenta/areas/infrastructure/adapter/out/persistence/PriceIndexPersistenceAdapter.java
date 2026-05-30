package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.magenta.areas.domain.model.*;
import com.magenta.areas.domain.port.in.CursorPage;
import com.magenta.areas.domain.port.out.PriceIndexRepositoryPort;
import com.magenta.areas.infrastructure.adapter.out.persistence.entity.PriceIndexJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PriceIndexPersistenceAdapter implements PriceIndexRepositoryPort {

    private final PriceIndexJpaRepository jpaRepo;

    public PriceIndexPersistenceAdapter(PriceIndexJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public void save(PriceIndex index) {
        jpaRepo.save(toEntity(index));
    }

    @Override
    public Optional<PriceIndex> findLatest(UUID zoneId, PropertyType propertyType,
                                            OperationType operationType) {
        return jpaRepo.findLatest(zoneId, propertyType.name(), operationType.name())
                .map(this::toDomain);
    }

    @Override
    public List<PriceIndex> findByZoneAndPeriod(UUID zoneId, PropertyType propertyType,
                                                  OperationType operationType,
                                                  LocalDate from, LocalDate to) {
        return jpaRepo.findByZoneAndPeriod(zoneId, propertyType.name(), operationType.name(), from, to)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public CursorPage<PriceIndex> findSeriesCursor(UUID zoneId, PropertyType propertyType,
                                                    OperationType operationType,
                                                    LocalDate from, LocalDate afterPeriod, int limit) {
        // sentinel: if afterPeriod is null, use a date before all data
        LocalDate after = afterPeriod != null ? afterPeriod : from.minusDays(1);
        // fetch limit+1 to detect next page
        List<PriceIndexJpaEntity> rows = jpaRepo.findSeriesCursor(
                zoneId, propertyType.name(), operationType.name(),
                from, after, PageRequest.of(0, limit + 1));
        boolean hasMore = rows.size() > limit;
        List<PriceIndex> items = rows.stream().limit(limit).map(this::toDomain).toList();
        String nextCursor = null;
        if (hasMore && !items.isEmpty()) {
            LocalDate lastPeriod = items.get(items.size() - 1).getPeriod();
            nextCursor = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(lastPeriod.toString().getBytes());
        }
        return new CursorPage<>(items, nextCursor);
    }

    /**
     * Las muestras de transacciones se obtienen de la tabla de price_indices con source != INTERNAL
     * que fueron importadas desde el topic magenta.products.transaction.v1.
     * En la implementación real se consultaría una tabla de staging; aquí devolvemos lo que existe.
     */
    @Override
    public List<BigDecimal> fetchTransactionSamples(UUID zoneId, PropertyType propertyType,
                                                     OperationType operationType, LocalDate period) {
        return jpaRepo.findByZoneAndPeriod(zoneId, propertyType.name(), operationType.name(),
                        period.withDayOfMonth(1), period.withDayOfMonth(period.lengthOfMonth()))
                .stream()
                .filter(e -> !SourceRef.INTERNAL.name().equals(e.getSource()))
                .map(PriceIndexJpaEntity::getPricePerSqm)
                .toList();
    }

    // ── mapping ──────────────────────────────────────────────────────────────

    private PriceIndex toDomain(PriceIndexJpaEntity e) {
        return PriceIndex.reconstitute(
                e.getId(), e.getTenantId(), e.getZoneId(),
                PropertyType.valueOf(e.getPropertyType()),
                OperationType.valueOf(e.getOperationType()),
                e.getPeriod(),
                Money.of(e.getPricePerSqm(), e.getCurrency()),
                e.getYoyDeltaPct(), e.getMomDeltaPct(),
                e.getSampleSize(), e.getConfidence(),
                SourceRef.valueOf(e.getSource()));
    }

    private PriceIndexJpaEntity toEntity(PriceIndex p) {
        PriceIndexJpaEntity e = new PriceIndexJpaEntity();
        e.setId(p.getId());
        e.setTenantId(p.getTenantId());
        e.setZoneId(p.getZoneId());
        e.setPropertyType(p.getPropertyType().name());
        e.setOperationType(p.getOperationType().name());
        e.setPeriod(p.getPeriod());
        e.setPricePerSqm(p.getPricePerSqm().amount());
        e.setCurrency(p.getPricePerSqm().currency());
        e.setYoyDeltaPct(p.getYoyDeltaPct());
        e.setMomDeltaPct(p.getMomDeltaPct());
        e.setSampleSize(p.getSampleSize());
        e.setConfidence(p.getConfidence());
        e.setSource(p.getSource().name());
        e.setCreatedAt(Instant.now());
        return e;
    }
}
