package com.magenta.areas.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.magenta.areas.domain.event.DomainEvent;
import com.magenta.areas.domain.event.ZoneCreated;
import com.magenta.areas.domain.event.ZoneDeprecated;
import com.magenta.areas.domain.event.ZoneUpdated;

/**
 * Aggregate Root del bounded context de Geografía.
 * Sin dependencias de Spring ni JPA.
 *
 * Invariantes:
 * - parentId debe pertenecer al ZoneType inmediatamente superior (validado por servicio).
 * - Un Zone DEPRECATED no acepta nuevos productos.
 */
public class Zone {

    private final UUID id;
    private final UUID tenantId;
    private String code;
    private String name;
    private ZoneType type;
    private UUID parentId;
    private String ineCode;
    private Set<String> postalCodes;
    private GeoPoint centroid;
    private String boundaryWkt;       // WKT del MULTIPOLYGON — persistido vía JPA entity
    private Integer population;
    private BigDecimal areaKm2;
    private ZoneStatus status;
    private Set<String> tags;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Zone(UUID id, UUID tenantId, String code, String name, ZoneType type,
                 UUID parentId, GeoPoint centroid, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.type = type;
        this.parentId = parentId;
        this.centroid = centroid;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.status = ZoneStatus.ACTIVE;
        this.postalCodes = new HashSet<>();
        this.tags = new HashSet<>();
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    public static Zone create(UUID tenantId, String code, String name, ZoneType type,
                              UUID parentId, GeoPoint centroid, String createdBy) {
        Zone zone = new Zone(UUID.randomUUID(), tenantId, code, name, type, parentId,
                centroid, Instant.now());
        zone.domainEvents.add(new ZoneCreated(zone.id, zone.code, zone.name,
                zone.type, zone.parentId, zone.centroid, zone.tenantId, createdBy));
        return zone;
    }

    /** Reconstruye desde persistencia — sin publicar evento. */
    public static Zone reconstitute(UUID id, UUID tenantId, String code, String name,
                                    ZoneType type, UUID parentId, String ineCode,
                                    Set<String> postalCodes, GeoPoint centroid,
                                    String boundaryWkt, Integer population,
                                    BigDecimal areaKm2, ZoneStatus status,
                                    Set<String> tags, Instant createdAt, Instant updatedAt) {
        Zone zone = new Zone(id, tenantId, code, name, type, parentId, centroid, createdAt);
        zone.ineCode = ineCode;
        zone.postalCodes = new HashSet<>(postalCodes);
        zone.boundaryWkt = boundaryWkt;
        zone.population = population;
        zone.areaKm2 = areaKm2;
        zone.status = status;
        zone.tags = new HashSet<>(tags);
        zone.updatedAt = updatedAt;
        return zone;
    }

    // ── Comportamiento de dominio ─────────────────────────────────────────────

    public void update(String newName, Set<String> newPostalCodes, Set<String> newTags,
                       Integer population, BigDecimal areaKm2, String updatedBy) {
        this.name = newName;
        this.postalCodes = new HashSet<>(newPostalCodes);
        this.tags = new HashSet<>(newTags);
        this.population = population;
        this.areaKm2 = areaKm2;
        this.updatedAt = Instant.now();
        domainEvents.add(new ZoneUpdated(id, code, name, type, parentId, centroid, tenantId, updatedBy));
    }

    public void deprecate(String updatedBy) {
        if (this.status == ZoneStatus.DEPRECATED) return;
        this.status = ZoneStatus.DEPRECATED;
        this.updatedAt = Instant.now();
        domainEvents.add(new ZoneDeprecated(id, code, name, type, tenantId, updatedBy));
    }

    public boolean isActive() {
        return status == ZoneStatus.ACTIVE;
    }

    // ── Eventos acumulados ────────────────────────────────────────────────────

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    // ── Getters (read-only) ───────────────────────────────────────────────────

    public UUID getId()              { return id; }
    public UUID getTenantId()        { return tenantId; }
    public String getCode()          { return code; }
    public String getName()          { return name; }
    public ZoneType getType()        { return type; }
    public UUID getParentId()        { return parentId; }
    public String getIneCode()       { return ineCode; }
    public Set<String> getPostalCodes() { return Collections.unmodifiableSet(postalCodes); }
    public GeoPoint getCentroid()    { return centroid; }
    public String getBoundaryWkt()   { return boundaryWkt; }
    public Integer getPopulation()   { return population; }
    public BigDecimal getAreaKm2()   { return areaKm2; }
    public ZoneStatus getStatus()    { return status; }
    public Set<String> getTags()     { return Collections.unmodifiableSet(tags); }
    public Instant getCreatedAt()    { return createdAt; }
    public Instant getUpdatedAt()    { return updatedAt; }

    public void setBoundaryWkt(String boundaryWkt) { this.boundaryWkt = boundaryWkt; }
    public void setIneCode(String ineCode)          { this.ineCode = ineCode; }
}
