package com.magenta.banks.domain.event;

import java.util.UUID;

public final class ProductPublished extends BanksDomainEvent {
    private final UUID productId;
    private final UUID bankId;
    private final String sku;
    private final String promoCode;

    public ProductPublished(UUID tenantId, UUID productId, UUID bankId, String sku, String promoCode) {
        super(tenantId);
        this.productId = productId;
        this.bankId    = bankId;
        this.sku       = sku;
        this.promoCode = promoCode;
    }

    @Override public String type()          { return "com.magenta.banks.ProductPublished"; }
    @Override public String aggregateName() { return "LoanProduct"; }
    @Override public UUID   aggregateId()   { return productId; }

    public UUID bankId()    { return bankId; }
    public String sku()     { return sku; }
    public String promoCode() { return promoCode; }
}
