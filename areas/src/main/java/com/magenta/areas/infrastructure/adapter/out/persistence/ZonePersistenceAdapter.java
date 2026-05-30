package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.magenta.areas.domain.model.*;
import com.magenta.areas.domain.port.in.CursorPage;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import com.magenta.areas.infrastructure.adapter.out.persistence.entity.ZoneJpaEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ZonePersistenceAdapter implements ZoneRepositoryPort {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private final ZoneJpaRepository jpaRepo;

    public ZonePersistenceAdapter(ZoneJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Zone save(Zone zone) {
        ZoneJpaEntity entity = toEntity(zone);
        ZoneJpaEntity saved  = jpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Zone> findById(UUID id) {
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Zone> findChildren(UUID parentId) {
        return jpaRepo.findByParentId(parentId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Zone> findAncestors(UUID id) {
        return jpaRepo.findAncestors(id).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Zone> searchByText(String text, List<ZoneType> types, int limit) {
        String typesArray = types.isEmpty() ? null
                : "{" + String.join(",", types.stream().map(Enum::name).toList()) + "}";
        return jpaRepo.searchByText(text, typesArray, limit).stream().map(this::toDomain).toList();
    }

    @Override
    public CursorPage<Zone> listCursor(String textFilter, List<ZoneType> types,
                                       int limit, String afterName, UUID afterId) {
        String typesArray = (types == null || types.isEmpty()) ? null
                : "{" + String.join(",", types.stream().map(Enum::name).toList()) + "}";
        // fetch limit+1 to detect whether there is a next page
        List<ZoneJpaEntity> rows = jpaRepo.listCursor(
                textFilter, typesArray,
                afterName, afterId != null ? afterId.toString() : null,
                limit + 1);
        boolean hasMore = rows.size() > limit;
        List<Zone> items = rows.stream().limit(limit).map(this::toDomain).toList();
        String nextCursor = null;
        if (hasMore && !items.isEmpty()) {
            Zone last = items.get(items.size() - 1);
            String raw = last.getName() + "|" + last.getId().toString();
            nextCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
        }
        return new CursorPage<>(items, nextCursor);
    }

    @Override
    public List<Zone> findByPostalCode(String postalCode) {
        return jpaRepo.findByPostalCode(postalCode).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Zone> resolvePoint(double lat, double lng) {
        return jpaRepo.resolvePoint(lat, lng).map(this::toDomain);
    }

    @Override
    public Optional<Zone> findByCode(String code) {
        return jpaRepo.findByCode(code).map(this::toDomain);
    }

    // ── mapping ──────────────────────────────────────────────────────────────

    private Zone toDomain(ZoneJpaEntity e) {
        Set<String> postalCodes = e.getPostalCodes() != null
                ? new HashSet<>(Arrays.asList(e.getPostalCodes())) : Set.of();
        Set<String> tags = e.getTags() != null
                ? new HashSet<>(Arrays.asList(e.getTags())) : Set.of();
        GeoPoint centroid = new GeoPoint(e.getCentroid().getY(), e.getCentroid().getX());

        Zone zone = Zone.reconstitute(
                e.getId(), e.getTenantId(), e.getCode(), e.getName(),
                ZoneType.valueOf(e.getType()), e.getParentId(),
                e.getIneCode(), postalCodes, centroid,
                e.getBoundary() != null ? e.getBoundary().toText() : null,
                e.getPopulation(), e.getAreaKm2(),
                ZoneStatus.valueOf(e.getStatus()), tags,
                e.getCreatedAt(), e.getUpdatedAt());
        return zone;
    }

    private ZoneJpaEntity toEntity(Zone z) {
        ZoneJpaEntity e = new ZoneJpaEntity();
        e.setId(z.getId());
        e.setTenantId(z.getTenantId());
        e.setCode(z.getCode());
        e.setName(z.getName());
        e.setType(z.getType().name());
        e.setParentId(z.getParentId());
        e.setIneCode(z.getIneCode());
        e.setPostalCodes(z.getPostalCodes().toArray(String[]::new));
        e.setTags(z.getTags().toArray(String[]::new));
        e.setCentroid(GF.createPoint(new Coordinate(z.getCentroid().lng(), z.getCentroid().lat())));
        if (z.getBoundaryWkt() != null) {
            try {
                e.setBoundary(new WKTReader(GF).read(z.getBoundaryWkt()));
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Invalid boundary WKT", ex);
            }
        }
        e.setPopulation(z.getPopulation());
        e.setAreaKm2(z.getAreaKm2());
        e.setStatus(z.getStatus().name());
        e.setCreatedAt(z.getCreatedAt());
        e.setUpdatedAt(z.getUpdatedAt());
        // created_by / updated_by — tomamos del último evento si está disponible
        e.setCreatedBy("system");
        e.setUpdatedBy("system");
        return e;
    }
}
