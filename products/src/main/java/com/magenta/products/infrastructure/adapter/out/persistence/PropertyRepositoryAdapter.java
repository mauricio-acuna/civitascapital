package com.magenta.products.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.products.domain.model.*;
import com.magenta.products.domain.port.out.PropertyRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PropertyRepositoryAdapter implements PropertyRepository {

    private static final GeometryFactory GEO_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private final PropertyJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public PropertyRepositoryAdapter(PropertyJpaRepository jpaRepository,
                                      ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Property save(Property property) {
        PropertyJpaEntity entity = toEntity(property);
        PropertyJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Property> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Property> findByTenantIdAndReference(UUID tenantId, String reference) {
        return jpaRepository.findByTenantIdAndReference(tenantId, reference).map(this::toDomain);
    }

    @Override
    public List<Property> findByZoneId(UUID zoneId) {
        return jpaRepository.findByZoneId(zoneId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByTenantIdAndReference(UUID tenantId, String reference) {
        return jpaRepository.existsByTenantIdAndReference(tenantId, reference);
    }

    // ── Mapping ────────────────────────────────────────────────────────────────

    private PropertyJpaEntity toEntity(Property p) {
        PropertyJpaEntity e = new PropertyJpaEntity();
        e.setId(p.id());
        e.setTenantId(p.tenantId());
        e.setReference(p.reference());
        e.setType(p.type().name());
        e.setSubtype(p.subtype());
        e.setStatus(p.status().name());
        e.setOwnerInfo(toJson(p.ownership()));
        e.setPostalCode(p.location().postalCode());
        e.setZoneId(p.location().zoneId());
        e.setVisibility(p.location().visibility().name());
        e.setAddress(buildAddressJson(p));
        e.setCoordinates(toPoint(p.location().coordinates()));
        e.setBuiltSqm(p.surface().builtSqm());
        e.setUsefulSqm(p.surface().usefulSqm());
        e.setPlotSqm(p.surface().plotSqm());
        if (p.layout() != null) {
            e.setRooms(p.layout().rooms());
            e.setBathrooms(p.layout().bathrooms());
            e.setTerraces(p.layout().terraces());
            e.setParkingSpots(p.layout().parkingSpots());
            e.setStorageRooms(p.layout().storageRooms());
            e.setFloor(p.layout().floor());
            e.setHasElevator(p.layout().hasElevator());
        }
        e.setCondition(p.condition() != null ? p.condition().name() : null);
        e.setBuildYear(p.buildYear());
        e.setLastRenovationYear(p.lastRenovationYear());
        e.setEnergy(toJson(p.energyRating()));
        e.setFeatures(p.features().toArray(new String[0]));
        e.setOrientation(p.orientation().stream().map(Orientation::name).toArray(String[]::new));
        e.setIte(toJson(p.ite()));
        e.setTags(p.tags().toArray(new String[0]));
        e.setFinancing(toJson(p.financing()));
        e.setPublishedAt(p.publishedAt());
        e.setCreatedAt(p.createdAt());
        e.setUpdatedAt(p.updatedAt());
        e.setCreatedBy(p.createdBy());
        e.setUpdatedBy(p.updatedBy());
        e.setVersion(p.version());
        return e;
    }

    private Property toDomain(PropertyJpaEntity e) {
        GeoPoint geoPoint = fromPoint(e.getCoordinates());
        Location location = new Location(
                extractStreet(e.getAddress()),
                extractNumber(e.getAddress()),
                e.getFloor() != null ? String.valueOf(e.getFloor()) : null,
                null,
                e.getPostalCode(),
                geoPoint,
                e.getZoneId(),
                null,
                LocationVisibility.valueOf(e.getVisibility()));

        Surface surface = new Surface(e.getBuiltSqm(), e.getUsefulSqm(), e.getPlotSqm());
        Layout layout = new Layout(e.getRooms(), e.getBathrooms(), e.getTerraces(),
                e.getParkingSpots(), e.getStorageRooms(), e.getFloor(), e.getHasElevator());

        Set<String> features = e.getFeatures() != null
                ? new LinkedHashSet<>(Arrays.asList(e.getFeatures())) : new LinkedHashSet<>();
        Set<Orientation> orientation = e.getOrientation() != null
                ? Arrays.stream(e.getOrientation()).map(Orientation::valueOf)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
                : new LinkedHashSet<>();
        Set<String> tags = e.getTags() != null
                ? new LinkedHashSet<>(Arrays.asList(e.getTags())) : new LinkedHashSet<>();

        Property p = Property.create(
                e.getId(), e.getTenantId(), e.getReference(),
                PropertyType.valueOf(e.getType()), e.getSubtype(),
                fromJson(e.getOwnerInfo(), OwnerInfo.class),
                location, surface, layout,
                e.getCondition() != null ? PropertyCondition.valueOf(e.getCondition()) : null,
                e.getBuildYear(),
                fromJson(e.getEnergy(), EnergyRating.class),
                features, orientation, tags, e.getCreatedBy());

        // Pull the creation event so reconstitution doesn't re-fire events
        p.pullDomainEvents();

        p.setStatus(PropertyStatus.valueOf(e.getStatus()));
        p.setPublishedAt(e.getPublishedAt());
        p.setCreatedAt(e.getCreatedAt());
        p.setUpdatedAt(e.getUpdatedAt());
        p.setUpdatedBy(e.getUpdatedBy());
        p.setVersion(e.getVersion());
        p.setFinancingInternal(fromJson(e.getFinancing(), FinancingHint.class));
        return p;
    }

    private org.locationtech.jts.geom.Point toPoint(GeoPoint gp) {
        org.locationtech.jts.geom.Point point =
                GEO_FACTORY.createPoint(new Coordinate(gp.lng(), gp.lat()));
        point.setSRID(4326);
        return point;
    }

    private GeoPoint fromPoint(org.locationtech.jts.geom.Point p) {
        return new GeoPoint(p.getY(), p.getX());
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception ex) { throw new IllegalStateException("JSON serialization failed", ex); }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        if (json == null) return null;
        try { return objectMapper.readValue(json, clazz); }
        catch (Exception ex) { throw new IllegalStateException("JSON deserialization failed", ex); }
    }

    private String buildAddressJson(Property p) {
        Map<String, String> addr = new HashMap<>();
        addr.put("street", p.location().street());
        addr.put("floor", p.location().floor());
        return toJson(addr);
    }

    private String extractStreet(String addressJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> m = objectMapper.readValue(addressJson, Map.class);
            return m != null ? m.get("street") : null;
        } catch (Exception e) { return null; }
    }

    private String extractNumber(String addressJson) {
        return null; // number stored encrypted, not in plain address json
    }
}
