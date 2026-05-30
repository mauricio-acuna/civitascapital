package com.magenta.areas.domain.event;

import com.magenta.areas.domain.model.Money;
import com.magenta.areas.domain.model.OperationType;
import com.magenta.areas.domain.model.PropertyType;

import java.time.LocalDate;
import java.util.UUID;

public final class PriceIndexPublished extends DomainEvent {

    private final UUID priceIndexId;
    private final UUID zoneId;
    private final PropertyType propertyType;
    private final OperationType operationType;
    private final LocalDate period;
    private final Money pricePerSqm;
    private final UUID tenantId;

    public PriceIndexPublished(UUID priceIndexId, UUID zoneId, PropertyType propertyType,
                                OperationType operationType, LocalDate period,
                                Money pricePerSqm, UUID tenantId, String actorId) {
        super(actorId);
        this.priceIndexId   = priceIndexId;
        this.zoneId         = zoneId;
        this.propertyType   = propertyType;
        this.operationType  = operationType;
        this.period         = period;
        this.pricePerSqm    = pricePerSqm;
        this.tenantId       = tenantId;
    }

    @Override public String getType() { return "com.magenta.areas.PriceIndexPublished"; }

    public UUID getPriceIndexId()        { return priceIndexId; }
    public UUID getZoneId()              { return zoneId; }
    public PropertyType getPropertyType(){ return propertyType; }
    public OperationType getOperationType(){ return operationType; }
    public LocalDate getPeriod()         { return period; }
    public Money getPricePerSqm()        { return pricePerSqm; }
    public UUID getTenantId()            { return tenantId; }
}
