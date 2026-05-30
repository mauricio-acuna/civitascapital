package com.magenta.banks.domain.event;

import java.util.UUID;

public final class PreapprovalApproved extends BanksDomainEvent {
    private final UUID preapprovalId;
    private final UUID customerId;

    public PreapprovalApproved(UUID tenantId, UUID preapprovalId, UUID customerId) {
        super(tenantId);
        this.preapprovalId = preapprovalId;
        this.customerId    = customerId;
    }

    @Override public String type()          { return "com.magenta.banks.PreapprovalApproved"; }
    @Override public String aggregateName() { return "Preapproval"; }
    @Override public UUID   aggregateId()   { return preapprovalId; }
    public UUID customerId() { return customerId; }
}
