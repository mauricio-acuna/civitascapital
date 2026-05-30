package com.magenta.areas.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "zones", schema = "areas")
@FilterDef(name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class ZoneJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, unique = true, length = 120)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "ine_code", length = 20)
    private String ineCode;

    @Column(name = "postal_codes", columnDefinition = "text[]")
    private String[] postalCodes;

    @Column(columnDefinition = "geography(POINT,4326)")
    private Point centroid;

    @Column(columnDefinition = "geography(MULTIPOLYGON,4326)")
    private Geometry boundary;

    private Integer population;

    @Column(name = "area_km2", precision = 12, scale = 4)
    private BigDecimal areaKm2;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    @Version
    private Long version;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ── Getters & setters ──────────────────────────────────────────────────────

    public UUID getId()              { return id; }
    public void setId(UUID id)       { this.id = id; }

    public UUID getTenantId()             { return tenantId; }
    public void setTenantId(UUID tenantId){ this.tenantId = tenantId; }

    public String getCode()          { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName()          { return name; }
    public void setName(String name) { this.name = name; }

    public String getType()          { return type; }
    public void setType(String type) { this.type = type; }

    public UUID getParentId()             { return parentId; }
    public void setParentId(UUID parentId){ this.parentId = parentId; }

    public String getIneCode()           { return ineCode; }
    public void setIneCode(String ineCode){ this.ineCode = ineCode; }

    public String[] getPostalCodes()                 { return postalCodes; }
    public void setPostalCodes(String[] postalCodes) { this.postalCodes = postalCodes; }

    public Point getCentroid()           { return centroid; }
    public void setCentroid(Point c)     { this.centroid = c; }

    public Geometry getBoundary()        { return boundary; }
    public void setBoundary(Geometry b)  { this.boundary = b; }

    public Integer getPopulation()               { return population; }
    public void setPopulation(Integer population){ this.population = population; }

    public BigDecimal getAreaKm2()           { return areaKm2; }
    public void setAreaKm2(BigDecimal km2)   { this.areaKm2 = km2; }

    public String getStatus()            { return status; }
    public void setStatus(String status) { this.status = status; }

    public String[] getTags()            { return tags; }
    public void setTags(String[] tags)   { this.tags = tags; }

    public Instant getCreatedAt()              { return createdAt; }
    public void setCreatedAt(Instant createdAt){ this.createdAt = createdAt; }

    public Instant getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt){ this.updatedAt = updatedAt; }

    public String getCreatedBy()             { return createdBy; }
    public void setCreatedBy(String createdBy){ this.createdBy = createdBy; }

    public String getUpdatedBy()             { return updatedBy; }
    public void setUpdatedBy(String updatedBy){ this.updatedBy = updatedBy; }

    public Long getVersion()           { return version; }
    public void setVersion(Long v)     { this.version = v; }

    public Instant getDeletedAt()              { return deletedAt; }
    public void setDeletedAt(Instant deletedAt){ this.deletedAt = deletedAt; }
}
