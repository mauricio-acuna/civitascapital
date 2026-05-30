package com.magenta.servicios.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Deliverable {

    private final UUID id;
    private final UUID orderId;
    private final DeliverableKind kind;
    private final String storageUri;
    private final String sha256;
    private final String signedBy;
    private final Instant issuedAt;

    public Deliverable(UUID id, UUID orderId, DeliverableKind kind,
                       String storageUri, String sha256, String signedBy, Instant issuedAt) {
        this.id = id;
        this.orderId = orderId;
        this.kind = kind;
        this.storageUri = storageUri;
        this.sha256 = sha256;
        this.signedBy = signedBy;
        this.issuedAt = issuedAt;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public DeliverableKind getKind() { return kind; }
    public String getStorageUri() { return storageUri; }
    public String getSha256() { return sha256; }
    public String getSignedBy() { return signedBy; }
    public Instant getIssuedAt() { return issuedAt; }
}
