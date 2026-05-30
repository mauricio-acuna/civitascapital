package com.magenta.servicios.infrastructure.adapter.out.persistence;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code services.partners}.
 *
 * <p>Los campos {@code services} y {@code coverage_zone_ids} son arrays de PostgreSQL.
 * Se mapean a {@code List<String>} / {@code List<UUID>} usando hypersistence-utils
 * {@link ListArrayType}, igual que {@code valid_for} en {@link ServiceDefinitionJpaEntity}.
 *
 * <p>{@code sepa_iban_enc} se almacena como {@code BYTEA} y su cifrado/descifrado
 * se gestiona fuera de esta entidad (Vault envelope encryption — pendiente S10).
 */
@Entity
@Table(name = "partners", schema = "services")
public class PartnerJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 20)
    private String kind;

    @Type(ListArrayType.class)
    @Column(columnDefinition = "TEXT[]")
    private List<String> services = new ArrayList<>();

    @Type(ListArrayType.class)
    @Column(name = "coverage_zone_ids", columnDefinition = "UUID[]")
    private List<UUID> coverageZoneIds = new ArrayList<>();

    @Column(name = "commission_pct", precision = 5, scale = 2)
    private BigDecimal commissionPct;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "nps_score")
    private Short npsScore;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "contract_ref", length = 120)
    private String contractRef;

    /** IBAN cifrado (Vault envelope encryption). Nulo hasta que se configure S10. */
    @Column(name = "sepa_iban_enc")
    private byte[] sepaIbanEnc;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

    public List<UUID> getCoverageZoneIds() { return coverageZoneIds; }
    public void setCoverageZoneIds(List<UUID> coverageZoneIds) { this.coverageZoneIds = coverageZoneIds; }

    public BigDecimal getCommissionPct() { return commissionPct; }
    public void setCommissionPct(BigDecimal commissionPct) { this.commissionPct = commissionPct; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Short getNpsScore() { return npsScore; }
    public void setNpsScore(Short npsScore) { this.npsScore = npsScore; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getContractRef() { return contractRef; }
    public void setContractRef(String contractRef) { this.contractRef = contractRef; }

    public byte[] getSepaIbanEnc() { return sepaIbanEnc; }
    public void setSepaIbanEnc(byte[] sepaIbanEnc) { this.sepaIbanEnc = sepaIbanEnc; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
