package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.magenta.areas.domain.model.DepopulationRisk;
import com.magenta.areas.domain.model.HospitalKind;
import com.magenta.areas.domain.model.ZoneEnrichment;
import com.magenta.areas.domain.port.out.EnrichmentRepositoryPort;
import com.magenta.areas.infrastructure.adapter.out.persistence.entity.ZoneEnrichmentJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class EnrichmentPersistenceAdapter implements EnrichmentRepositoryPort {

    private final ZoneEnrichmentJpaRepository jpaRepo;

    public EnrichmentPersistenceAdapter(ZoneEnrichmentJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public void save(ZoneEnrichment enrichment) {
        jpaRepo.save(toEntity(enrichment));
    }

    @Override
    public Optional<ZoneEnrichment> findByZoneId(UUID zoneId) {
        return jpaRepo.findById(zoneId).map(this::toDomain);
    }

    // ── mapping ──────────────────────────────────────────────────────────────

    private ZoneEnrichment toDomain(ZoneEnrichmentJpaEntity e) {
        return ZoneEnrichment.reconstitute(
                e.getZoneId(), e.getTenantId(),
                e.getFiberCoveragePct(), e.isHasHospital(),
                HospitalKind.valueOf(e.getHospitalKind()),
                e.getTrainToHubMinutes(), e.getHighwayDistanceKm(),
                e.getSupermarketsCount(), e.getRiskOccupationScore(),
                DepopulationRisk.valueOf(e.getDepopulationRisk()),
                e.getQualityOfLifeIndex(), e.getUpdatedAt());
    }

    private ZoneEnrichmentJpaEntity toEntity(ZoneEnrichment z) {
        ZoneEnrichmentJpaEntity e = new ZoneEnrichmentJpaEntity();
        e.setZoneId(z.getZoneId());
        e.setTenantId(z.getTenantId());
        e.setFiberCoveragePct(z.getFiberCoveragePct());
        e.setHasHospital(z.isHasHospital());
        e.setHospitalKind(z.getHospitalKind().name());
        e.setTrainToHubMinutes(z.getTrainToHubMinutes());
        e.setHighwayDistanceKm(z.getHighwayDistanceKm());
        e.setSupermarketsCount(z.getSupermarketsCount());
        e.setRiskOccupationScore(z.getRiskOccupationScore());
        e.setDepopulationRisk(z.getDepopulationRisk().name());
        e.setQualityOfLifeIndex(z.getQualityOfLifeIndex());
        e.setUpdatedAt(z.getUpdatedAt());
        return e;
    }
}
