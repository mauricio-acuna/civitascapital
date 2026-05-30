package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.magenta.areas.infrastructure.adapter.out.persistence.entity.ZoneJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZoneJpaRepository extends JpaRepository<ZoneJpaEntity, UUID> {

    List<ZoneJpaEntity> findByParentId(UUID parentId);

    Optional<ZoneJpaEntity> findByCode(String code);

    @Query(value = """
        SELECT * FROM areas.zones
        WHERE :postalCode = ANY(postal_codes)
          AND deleted_at IS NULL
        """, nativeQuery = true)
    List<ZoneJpaEntity> findByPostalCode(@Param("postalCode") String postalCode);

    @Query(value = """
        SELECT * FROM areas.zones
        WHERE (search_vector @@ plainto_tsquery('spanish', unaccent(:query))
               OR name % :query)
          AND (:types IS NULL OR type = ANY(CAST(:types AS VARCHAR[])))
          AND deleted_at IS NULL
        ORDER BY ts_rank(search_vector, plainto_tsquery('spanish', unaccent(:query))) DESC,
                 similarity(name, :query) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<ZoneJpaEntity> searchByText(@Param("query") String query,
                                      @Param("types") String types,
                                      @Param("limit") int limit);

    /**
     * Keyset pagination: ordena por (name ASC, id ASC).
     * Cuando cursorName y cursorId son null se trata como primera página.
     */
    @Query(value = """
        SELECT * FROM areas.zones
        WHERE (:textFilter IS NULL
               OR search_vector @@ plainto_tsquery('spanish', unaccent(:textFilter))
               OR name % :textFilter)
          AND (:types IS NULL OR type = ANY(CAST(:types AS VARCHAR[])))
          AND (:cursorName IS NULL
               OR name > :cursorName
               OR (name = :cursorName AND id > CAST(:cursorId AS uuid)))
          AND deleted_at IS NULL
        ORDER BY name ASC, id ASC
        LIMIT :lim
        """, nativeQuery = true)
    List<ZoneJpaEntity> listCursor(@Param("textFilter") String textFilter,
                                    @Param("types") String types,
                                    @Param("cursorName") String cursorName,
                                    @Param("cursorId") String cursorId,
                                    @Param("lim") int lim);

    /**
     * Resolución espacial: devuelve la zona más específica que contiene el punto (§9.2 del spec).
     */
    @Query(value = """
        SELECT * FROM areas.zones
        WHERE ST_Covers(boundary, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)
          AND status = 'ACTIVE'
          AND deleted_at IS NULL
        ORDER BY CASE type
           WHEN 'BUILDING'      THEN 1
           WHEN 'STREET'        THEN 2
           WHEN 'URBANIZATION'  THEN 3
           WHEN 'NEIGHBORHOOD'  THEN 4
           WHEN 'DISTRICT'      THEN 5
           WHEN 'MUNICIPALITY'  THEN 6
           ELSE 9
        END
        LIMIT 1
        """, nativeQuery = true)
    Optional<ZoneJpaEntity> resolvePoint(@Param("lat") double lat, @Param("lng") double lng);

    /**
     * Cadena de ancestros ordenada de root a padre inmediato — CTE recursivo.
     */
    @Query(value = """
        WITH RECURSIVE ancestors AS (
            SELECT z.*, 0 AS depth
            FROM areas.zones z
            WHERE z.id = (SELECT parent_id FROM areas.zones WHERE id = :zoneId)
            UNION ALL
            SELECT z.*, a.depth + 1
            FROM areas.zones z
            JOIN ancestors a ON z.id = a.parent_id
        )
        SELECT * FROM ancestors ORDER BY depth DESC
        """, nativeQuery = true)
    List<ZoneJpaEntity> findAncestors(@Param("zoneId") UUID zoneId);
}
