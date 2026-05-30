package com.magenta.areas.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event", schema = "areas")
public class OutboxEventJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 64)
    private String aggregate;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(nullable = false, length = 120)
    private String type;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    // ── Getters & setters ──────────────────────────────────────────────────────

    public UUID getId()                        { return id; }
    public void setId(UUID id)                 { this.id = id; }
    public String getAggregate()               { return aggregate; }
    public void setAggregate(String a)         { this.aggregate = a; }
    public UUID getAggregateId()               { return aggregateId; }
    public void setAggregateId(UUID a)         { this.aggregateId = a; }
    public String getType()                    { return type; }
    public void setType(String t)              { this.type = t; }
    public String getPayload()                 { return payload; }
    public void setPayload(String p)           { this.payload = p; }
    public UUID getTenantId()                  { return tenantId; }
    public void setTenantId(UUID t)            { this.tenantId = t; }
    public Instant getCreatedAt()              { return createdAt; }
    public void setCreatedAt(Instant c)        { this.createdAt = c; }
    public Instant getPublishedAt()            { return publishedAt; }
    public void setPublishedAt(Instant p)      { this.publishedAt = p; }
}
