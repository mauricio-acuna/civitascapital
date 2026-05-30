package com.magenta.banks.domain.event;

import java.util.UUID;

public final class ProductUpdated extends BanksDomainEvent {
    private final UUID productId;

    public ProductUpdated(UUID tenantId, UUID productId) {
        super(tenantId);
        this.productId = productId;
    }

    @Override public String type()          { return "com.magenta.banks.ProductUpdated"; }
    @Override public String aggregateName() { return "LoanProduct"; }
    @Override public UUID   aggregateId()   { return productId; }
}
