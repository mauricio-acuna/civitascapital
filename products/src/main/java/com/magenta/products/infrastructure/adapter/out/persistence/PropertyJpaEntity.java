package com.magenta.products.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.ParamDef;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "properties", schema = "products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "reference"}))
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter @Setter
public class PropertyJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "reference", nullable = false, length = 80)
    private String reference;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "subtype", length = 80)
    private String subtype;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "owner_info", columnDefinition = "jsonb")
    private String ownerInfo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address", columnDefinition = "jsonb")
    private String address;

    @Column(name = "address_exact_enc")
    private byte[] addressExactEnc;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "coordinates", nullable = false, columnDefinition = "geography(POINT,4326)")
    private Point coordinates;

    @Column(name = "zone_id", nullable = false)
    private UUID zoneId;

    @Column(name = "visibility", nullable = false, length = 24)
    private String visibility;

    @Column(name = "built_sqm", nullable = false, precision = 8, scale = 2)
    private BigDecimal builtSqm;

    @Column(name = "useful_sqm", precision = 8, scale = 2)
    private BigDecimal usefulSqm;

    @Column(name = "plot_sqm", precision = 10, scale = 2)
    private BigDecimal plotSqm;

    @Column(name = "rooms")
    private Integer rooms;

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "terraces")
    private Integer terraces;

    @Column(name = "parking_spots")
    private Integer parkingSpots;

    @Column(name = "storage_rooms")
    private Integer storageRooms;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "has_elevator")
    private Boolean hasElevator;

    @Column(name = "condition", length = 20)
    private String condition;

    @Column(name = "build_year")
    private Integer buildYear;

    @Column(name = "last_reno_year")
    private Integer lastRenovationYear;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "energy", columnDefinition = "jsonb")
    private String energy;

    @Column(name = "features", columnDefinition = "text[]")
    private String[] features;

    @Column(name = "orientation", columnDefinition = "text[]")
    private String[] orientation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ite", columnDefinition = "jsonb")
    private String ite;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "financing", columnDefinition = "jsonb")
    private String financing;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, length = 64, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.LAZY)
    @OrderBy("order ASC")
    private List<MediaAssetJpaEntity> mediaAssets = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<OperationJpaEntity> operations = new ArrayList<>();
}
