package com.magenta.servicios.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PartnerJpaRepository extends JpaRepository<PartnerJpaEntity, UUID> {

    List<PartnerJpaEntity> findByTenantIdAndActiveTrue(UUID tenantId);

    /**
     * Busca partners activos que cubran el serviceCode y la zona indicados.
     * Usa los operadores de arrays de PostgreSQL {@code @>} (contains).
     */
    @Query(value = """
            SELECT p.* FROM services.partners p
            WHERE p.active = true
              AND p.services @> ARRAY[:serviceCode]::TEXT[]
              AND p.coverage_zone_ids @> ARRAY[:zoneId]::UUID[]
            """, nativeQuery = true)
    List<PartnerJpaEntity> findActiveByServiceAndZone(
            @Param("serviceCode") String serviceCode,
            @Param("zoneId") UUID zoneId);
}
