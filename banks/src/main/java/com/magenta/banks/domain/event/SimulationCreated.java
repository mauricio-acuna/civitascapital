package com.magenta.banks.domain.event;

import java.util.UUID;

public final class SimulationCreated extends BanksDomainEvent {
    private final UUID simulationId;
    private final UUID customerId;
    private final UUID productId;

    public SimulationCreated(UUID tenantId, UUID simulationId, UUID customerId, UUID productId) {
        super(tenantId);
        this.simulationId = simulationId;
        this.customerId   = customerId;
        this.productId    = productId;
    }

    @Override public String type()          { return "com.magenta.banks.SimulationCreated"; }
    @Override public String aggregateName() { return "LoanSimulation"; }
    @Override public UUID   aggregateId()   { return simulationId; }
    public UUID customerId() { return customerId; }
    public UUID productId()  { return productId; }
}
