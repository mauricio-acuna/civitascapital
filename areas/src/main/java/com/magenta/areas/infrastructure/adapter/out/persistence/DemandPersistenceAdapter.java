package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.magenta.areas.domain.model.ZoneDemandSnapshot;
import com.magenta.areas.domain.port.out.DemandRepositoryPort;
import com.magenta.areas.infrastructure.adapter.out.persistence.entity.ZoneDemandSnapshotJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class DemandPersistenceAdapter implements DemandRepositoryPort {

    private final ZoneDemandJpaRepository demandRepo;

    public DemandPersistenceAdapter(ZoneDemandJpaRepository demandRepo) {
        this.demandRepo = demandRepo;
    }

    @Override
    public void save(ZoneDemandSnapshot snapshot) {
        demandRepo.save(toEntity(snapshot));
    }

    @Override
    public Optional<ZoneDemandSnapshot> findByZoneAndPeriod(UUID zoneId, LocalDate period) {
        return demandRepo.findByZoneIdAndPeriod(zoneId, period)
                .map(this::toDomain);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private ZoneDemandSnapshotJpaEntity toEntity(ZoneDemandSnapshot s) {
        ZoneDemandSnapshotJpaEntity e = new ZoneDemandSnapshotJpaEntity();
        e.setId(s.getId());
        e.setZoneId(s.getZoneId());
        e.setPeriod(s.getPeriod());
        e.setSearches(s.getSearches());
        e.setLeads(s.getLeads());
        e.setViewedProperties(s.getViewedProperties());
        e.setSavedSearches(s.getSavedSearches());
        e.setSupplyDemandRatio(s.getSupplyDemandRatio());
        return e;
    }

    private ZoneDemandSnapshot toDomain(ZoneDemandSnapshotJpaEntity e) {
        return ZoneDemandSnapshot.reconstitute(
                e.getId(),
                e.getZoneId(),
                e.getPeriod(),
                e.getSearches(),
                e.getLeads(),
                e.getViewedProperties(),
                e.getSavedSearches(),
                e.getSupplyDemandRatio()
        );
    }
}
