package com.magenta.banks.domain.event;

import java.util.UUID;

public final class PreapprovalExpired extends BanksDomainEvent {
    private final UUID preapprovalId;
    private final UUID customerId;

    public PreapprovalExpired(UUID tenantId, UUID preapprovalId, UUID customerId) {
        super(tenantId);
        this.preapprovalId = preapprovalId;
        this.customerId    = customerId;
    }

    @Override public String type()          { return "com.magenta.banks.PreapprovalExpired"; }
    @Override public String aggregateName() { return "Preapproval"; }
    @Override public UUID   aggregateId()   { return preapprovalId; }
    public UUID customerId() { return customerId; }
}
