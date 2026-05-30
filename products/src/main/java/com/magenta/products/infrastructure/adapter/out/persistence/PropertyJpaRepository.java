package com.magenta.products.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyJpaRepository extends JpaRepository<PropertyJpaEntity, UUID> {

    Optional<PropertyJpaEntity> findByTenantIdAndReference(UUID tenantId, String reference);

    Optional<PropertyJpaEntity> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndReference(UUID tenantId, String reference);

    List<PropertyJpaEntity> findByZoneId(UUID zoneId);

    List<PropertyJpaEntity> findByTenantIdAndZoneId(UUID tenantId, UUID zoneId);

    @Query("SELECT p FROM PropertyJpaEntity p WHERE p.status = :status AND p.deletedAt IS NULL")
    List<PropertyJpaEntity> findByStatus(@Param("status") String status);

    @Query("""
        SELECT DISTINCT p FROM PropertyJpaEntity p
        LEFT JOIN p.operations o
        WHERE p.tenantId = :tenantId
          AND p.deletedAt IS NULL
          AND (:status IS NULL OR p.status = :status)
          AND (:type IS NULL OR p.type = :type)
          AND (:zoneId IS NULL OR p.zoneId = :zoneId)
          AND (:operationType IS NULL OR o.type = :operationType)
          AND (:minPrice IS NULL OR o.price >= :minPrice)
          AND (:maxPrice IS NULL OR o.price <= :maxPrice)
        ORDER BY p.publishedAt DESC, p.updatedAt DESC
        """)
    List<PropertyJpaEntity> search(
            @Param("tenantId") UUID tenantId,
            @Param("status") String status,
            @Param("type") String type,
            @Param("zoneId") UUID zoneId,
            @Param("operationType") String operationType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
