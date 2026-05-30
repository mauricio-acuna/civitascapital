package com.magenta.banks.domain.event;

import java.util.UUID;

public final class PreapprovalRejected extends BanksDomainEvent {
    private final UUID preapprovalId;
    private final UUID customerId;
    private final String reason;

    public PreapprovalRejected(UUID tenantId, UUID preapprovalId, UUID customerId, String reason) {
        super(tenantId);
        this.preapprovalId = preapprovalId;
        this.customerId    = customerId;
        this.reason        = reason;
    }

    @Override public String type()          { return "com.magenta.banks.PreapprovalRejected"; }
    @Override public String aggregateName() { return "Preapproval"; }
    @Override public UUID   aggregateId()   { return preapprovalId; }
    public UUID customerId() { return customerId; }
    public String reason()   { return reason; }
}
