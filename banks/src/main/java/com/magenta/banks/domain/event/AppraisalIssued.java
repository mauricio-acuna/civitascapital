package com.magenta.banks.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class AppraisalIssued extends BanksDomainEvent {
    private final UUID appraisalId;
    private final UUID propertyId;
    private final BigDecimal marketValue;
    private final BigDecimal mortgageValue;
    private final LocalDate validUntil;

    public AppraisalIssued(UUID tenantId, UUID appraisalId, UUID propertyId,
                           BigDecimal marketValue, BigDecimal mortgageValue, LocalDate validUntil) {
        super(tenantId);
        this.appraisalId    = appraisalId;
        this.propertyId     = propertyId;
        this.marketValue    = marketValue;
        this.mortgageValue  = mortgageValue;
        this.validUntil     = validUntil;
    }

    @Override public String type()          { return "com.magenta.banks.AppraisalIssued"; }
    @Override public String aggregateName() { return "Appraisal"; }
    @Override public UUID   aggregateId()   { return appraisalId; }
    public UUID propertyId()          { return propertyId; }
    public BigDecimal marketValue()   { return marketValue; }
    public BigDecimal mortgageValue() { return mortgageValue; }
    public LocalDate validUntil()     { return validUntil; }
}
