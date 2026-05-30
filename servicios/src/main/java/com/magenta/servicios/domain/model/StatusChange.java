package com.magenta.servicios.domain.model;

import java.time.Instant;
import java.util.UUID;

public class StatusChange {

    private final UUID id;
    private final UUID orderId;
    private final OrderStatus fromStatus;
    private final OrderStatus toStatus;
    private final String reason;
    private final String actor;
    private final String payload;
    private final Instant at;

    public StatusChange(UUID id, UUID orderId, OrderStatus fromStatus, OrderStatus toStatus,
                        String reason, String actor, String payload, Instant at) {
        this.id = id;
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.actor = actor;
        this.payload = payload;
        this.at = at;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public OrderStatus getFromStatus() { return fromStatus; }
    public OrderStatus getToStatus() { return toStatus; }
    public String getReason() { return reason; }
    public String getActor() { return actor; }
    public String getPayload() { return payload; }
    public Instant getAt() { return at; }
}
