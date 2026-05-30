package com.magenta.servicios.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderJpaEntity, UUID> {

    Optional<ServiceOrderJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<ServiceOrderJpaEntity> findByCustomerIdAndTenantId(UUID customerId, UUID tenantId, Pageable pageable);

    Page<ServiceOrderJpaEntity> findByStatusAndTenantId(String status, UUID tenantId, Pageable pageable);

    @Query("""
            SELECT o FROM ServiceOrderJpaEntity o
            WHERE o.slaDueAt < :now
              AND o.status NOT IN ('COMPLETED', 'CANCELLED', 'FAILED')
              AND o.deletedAt IS NULL
            """)
    List<ServiceOrderJpaEntity> findOverdueSla(@Param("now") Instant now);
}
