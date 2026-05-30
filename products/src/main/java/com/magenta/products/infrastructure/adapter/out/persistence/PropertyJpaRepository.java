package com.magenta.products.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyJpaRepository extends JpaRepository<PropertyJpaEntity, UUID> {

    Optional<PropertyJpaEntity> findByTenantIdAndReference(UUID tenantId, String reference);

    boolean existsByTenantIdAndReference(UUID tenantId, String reference);

    List<PropertyJpaEntity> findByZoneId(UUID zoneId);

    List<PropertyJpaEntity> findByTenantIdAndZoneId(UUID tenantId, UUID zoneId);

    @Query("SELECT p FROM PropertyJpaEntity p WHERE p.status = :status AND p.deletedAt IS NULL")
    List<PropertyJpaEntity> findByStatus(@Param("status") String status);
}
