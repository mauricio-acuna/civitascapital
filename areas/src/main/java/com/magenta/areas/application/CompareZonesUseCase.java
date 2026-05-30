package com.magenta.areas.application;

import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.PriceIndex;
import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.model.ZoneEnrichment;
import com.magenta.areas.domain.model.OperationType;
import com.magenta.areas.domain.model.PropertyType;
import com.magenta.areas.domain.port.in.CompareZonesPort;
import com.magenta.areas.domain.port.out.EnrichmentRepositoryPort;
import com.magenta.areas.domain.port.out.PriceIndexRepositoryPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CompareZonesUseCase implements CompareZonesPort {

    private static final int MAX_ZONES = 5;

    private final ZoneRepositoryPort zoneRepo;
    private final PriceIndexRepositoryPort priceRepo;
    private final EnrichmentRepositoryPort enrichmentRepo;

    public CompareZonesUseCase(ZoneRepositoryPort zoneRepo,
                                PriceIndexRepositoryPort priceRepo,
                                EnrichmentRepositoryPort enrichmentRepo) {
        this.zoneRepo        = zoneRepo;
        this.priceRepo       = priceRepo;
        this.enrichmentRepo  = enrichmentRepo;
    }

    @Override
    @Cacheable(value = "zoneComparisons", key = "#zoneIds.stream().sorted().toList().toString()")
    public List<ZoneComparison> execute(List<UUID> zoneIds) {
        if (zoneIds == null || zoneIds.isEmpty() || zoneIds.size() > MAX_ZONES) {
            throw new IllegalArgumentException("Compare requires between 2 and " + MAX_ZONES + " zones");
        }

        return zoneIds.stream().map(zoneId -> {
            Zone zone = zoneRepo.findById(zoneId)
                    .orElseThrow(() -> new ZoneNotFoundException(zoneId));
            PriceIndex sale = priceRepo.findLatest(zoneId, PropertyType.FLAT, OperationType.SALE)
                    .orElse(null);
            PriceIndex rent = priceRepo.findLatest(zoneId, PropertyType.FLAT, OperationType.RENT)
                    .orElse(null);
            ZoneEnrichment enrichment = enrichmentRepo.findByZoneId(zoneId)
                    .orElse(ZoneEnrichment.empty(zoneId, zone.getTenantId()));
            return new ZoneComparison(zone, sale, rent, enrichment);
        }).toList();
    }
}
