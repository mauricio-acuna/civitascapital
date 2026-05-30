package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.magenta.areas.infrastructure.adapter.out.persistence.entity.PriceIndexJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

public interface PriceIndexJpaRepository extends JpaRepository<PriceIndexJpaEntity, UUID> {

    @Query("""
        SELECT p FROM PriceIndexJpaEntity p
        WHERE p.zoneId = :zoneId
          AND p.propertyType = :propertyType
          AND p.operationType = :operationType
        ORDER BY p.period DESC
        LIMIT 1
        """)
    Optional<PriceIndexJpaEntity> findLatest(@Param("zoneId") UUID zoneId,
                                              @Param("propertyType") String propertyType,
                                              @Param("operationType") String operationType);

    @Query("""
        SELECT p FROM PriceIndexJpaEntity p
        WHERE p.zoneId = :zoneId
          AND p.propertyType = :propertyType
          AND p.operationType = :operationType
          AND p.period BETWEEN :from AND :to
        ORDER BY p.period ASC
        """)
    List<PriceIndexJpaEntity> findByZoneAndPeriod(@Param("zoneId") UUID zoneId,
                                                   @Param("propertyType") String propertyType,
                                                   @Param("operationType") String operationType,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    /**
     * Keyset pagination: devuelve hasta {@code pageable.getPageSize()} filas con period > afterPeriod.
     * Pasar {@code afterPeriod = from.minusDays(1)} en la primera página.
     */
    @Query("""
        SELECT p FROM PriceIndexJpaEntity p
        WHERE p.zoneId = :zoneId
          AND p.propertyType = :propertyType
          AND p.operationType = :operationType
          AND p.period >= :from
          AND p.period > :afterPeriod
        ORDER BY p.period ASC
        """)
    List<PriceIndexJpaEntity> findSeriesCursor(@Param("zoneId") UUID zoneId,
                                                @Param("propertyType") String propertyType,
                                                @Param("operationType") String operationType,
                                                @Param("from") LocalDate from,
                                                @Param("afterPeriod") LocalDate afterPeriod,
                                                Pageable pageable);
}
