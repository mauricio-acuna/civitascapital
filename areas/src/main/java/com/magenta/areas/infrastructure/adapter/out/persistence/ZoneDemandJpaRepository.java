package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.magenta.areas.infrastructure.adapter.out.persistence.entity.ZoneDemandSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ZoneDemandJpaRepository extends JpaRepository<ZoneDemandSnapshotJpaEntity, UUID> {

    Optional<ZoneDemandSnapshotJpaEntity> findByZoneIdAndPeriod(UUID zoneId, LocalDate period);
}
