package com.magenta.banks.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PreapprovalRequested extends BanksDomainEvent {
    private final UUID preapprovalId;
    private final UUID customerId;
    private final UUID productId;
    private final UUID propertyId;
    private final BigDecimal amount;
    private final BigDecimal ltv;
    private final Instant expiresAt;

    public PreapprovalRequested(UUID tenantId, UUID preapprovalId, UUID customerId,
                                UUID productId, UUID propertyId,
                                BigDecimal amount, BigDecimal ltv, Instant expiresAt) {
        super(tenantId);
        this.preapprovalId = preapprovalId;
        this.customerId    = customerId;
        this.productId     = productId;
        this.propertyId    = propertyId;
        this.amount        = amount;
        this.ltv           = ltv;
        this.expiresAt     = expiresAt;
    }

    @Override public String type()          { return "com.magenta.banks.PreapprovalRequested"; }
    @Override public String aggregateName() { return "Preapproval"; }
    @Override public UUID   aggregateId()   { return preapprovalId; }

    public UUID customerId()  { return customerId; }
    public UUID productId()   { return productId; }
    public UUID propertyId()  { return propertyId; }
    public BigDecimal amount() { return amount; }
    public BigDecimal ltv()   { return ltv; }
    public Instant expiresAt() { return expiresAt; }
}
