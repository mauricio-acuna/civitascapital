package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.magenta.areas.infrastructure.adapter.out.persistence.entity.ZoneEnrichmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ZoneEnrichmentJpaRepository extends JpaRepository<ZoneEnrichmentJpaEntity, UUID> {
}
