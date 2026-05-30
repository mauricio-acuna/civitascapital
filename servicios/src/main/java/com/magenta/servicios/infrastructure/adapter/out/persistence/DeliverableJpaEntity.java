package com.magenta.servicios.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code services.deliverables}.
 * Append-only: no tiene campo {@code version} ni {@code updated_at}.
 */
@Entity
@Table(name = "deliverables", schema = "services")
public class DeliverableJpaEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(nullable = false, length = 24)
    private String kind;

    @Column(name = "storage_uri", nullable = false, columnDefinition = "TEXT")
    private String storageUri;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(name = "signed_by", length = 160)
    private String signedBy;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getStorageUri() { return storageUri; }
    public void setStorageUri(String storageUri) { this.storageUri = storageUri; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public String getSignedBy() { return signedBy; }
    public void setSignedBy(String signedBy) { this.signedBy = signedBy; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
}
