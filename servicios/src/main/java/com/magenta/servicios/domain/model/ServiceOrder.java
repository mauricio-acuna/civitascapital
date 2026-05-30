package com.magenta.servicios.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root: instancia de un servicio contratado por un cliente.
 * Toda transición de estado emite un DomainEvent.
 */
public class ServiceOrder {

    private final UUID id;
    private final UUID tenantId;
    private final ServiceCode serviceCode;
    private final UUID customerId;
    private UUID propertyId;
    private UUID operationId;
    private UUID bankProductId;
    private String inputs;                  // JSONB
    private BigDecimal priceQuoted;
    private BigDecimal priceFinal;
    private final String currency;
    private OrderStatus status;
    private String workflowInstanceId;
    private UUID partnerId;
    private Instant slaDueAt;
    private final List<Deliverable> deliverables;
    private final List<Payment> payments;
    private final List<StatusChange> history;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private long version;

    private final List<Object> domainEvents = new ArrayList<>();

    public ServiceOrder(UUID id, UUID tenantId, ServiceCode serviceCode, UUID customerId,
                        UUID propertyId, UUID operationId, UUID bankProductId,
                        String inputs, BigDecimal priceQuoted, String currency) {
        this.id = id;
        this.tenantId = tenantId;
        this.serviceCode = serviceCode;
        this.customerId = customerId;
        this.propertyId = propertyId;
        this.operationId = operationId;
        this.bankProductId = bankProductId;
        this.inputs = inputs;
        this.priceQuoted = priceQuoted;
        this.currency = currency;
        this.status = OrderStatus.DRAFT;
        this.deliverables = new ArrayList<>();
        this.payments = new ArrayList<>();
        this.history = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    // ── Reconstitución desde persistencia ───────────────────────────────────
    public static ServiceOrder reconstitute(
            UUID id, UUID tenantId, ServiceCode serviceCode, UUID customerId,
            UUID propertyId, UUID operationId, UUID bankProductId,
            String inputs, BigDecimal priceQuoted, BigDecimal priceFinal, String currency,
            OrderStatus status, String workflowInstanceId, UUID partnerId, Instant slaDueAt,
            List<Deliverable> deliverables, List<Payment> payments, List<StatusChange> history,
            Instant createdAt, Instant updatedAt, Instant completedAt, long version) {

        ServiceOrder order = new ServiceOrder(id, tenantId, serviceCode, customerId,
                propertyId, operationId, bankProductId, inputs, priceQuoted, currency);
        order.priceFinal = priceFinal;
        order.status = status;
        order.workflowInstanceId = workflowInstanceId;
        order.partnerId = partnerId;
        order.slaDueAt = slaDueAt;
        order.deliverables.addAll(deliverables != null ? deliverables : List.of());
        order.payments.addAll(payments != null ? payments : List.of());
        order.history.addAll(history != null ? history : List.of());
        // overwrite audit fields set in constructor
        try {
            var createdAtField = ServiceOrder.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(order, createdAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // no-op: fields remain as constructor set
        }
        order.updatedAt = updatedAt;
        order.completedAt = completedAt;
        order.version = version;
        return order;
    }

    // ── Transiciones de estado ───────────────────────────────────────────────

    public void quote(BigDecimal price, Instant slaDue, String actor) {
        requireStatus(OrderStatus.DRAFT);
        this.priceQuoted = price;
        this.slaDueAt = slaDue;
        transition(OrderStatus.QUOTED, "Precio calculado", actor, null);
    }

    public void accept(String actor) {
        requireStatus(OrderStatus.QUOTED);
        transition(OrderStatus.ACCEPTED, "Aceptado por cliente", actor, null);
    }

    public void startWorkflow(String workflowInstanceId, String actor) {
        requireStatus(OrderStatus.ACCEPTED);
        this.workflowInstanceId = workflowInstanceId;
        transition(OrderStatus.IN_PROGRESS, "Workflow iniciado", actor, null);
    }

    public void complete(BigDecimal priceFinal, String actor) {
        requireStatus(OrderStatus.IN_PROGRESS);
        this.priceFinal = priceFinal;
        this.completedAt = Instant.now();
        transition(OrderStatus.COMPLETED, "Completado", actor, null);
    }

    public void cancel(String reason, String actor) {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.FAILED) {
            throw new IllegalStateException("No se puede cancelar una orden en estado " + status);
        }
        transition(OrderStatus.CANCELLED, reason, actor, null);
    }

    public void fail(String reason, String actor) {
        transition(OrderStatus.FAILED, reason, actor, null);
    }

    public void assignPartner(UUID partnerId, String actor) {
        this.partnerId = partnerId;
        this.updatedAt = Instant.now();
        history.add(new StatusChange(UUID.randomUUID(), id, status, status,
                "Partner asignado: " + partnerId, actor, null, Instant.now()));
    }

    public void addDeliverable(Deliverable deliverable) {
        deliverables.add(deliverable);
        this.updatedAt = Instant.now();
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        this.updatedAt = Instant.now();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void requireStatus(OrderStatus expected) {
        if (!expected.equals(this.status)) {
            throw new IllegalStateException(
                    "Se esperaba estado " + expected + " pero la orden está en " + this.status);
        }
    }

    private void transition(OrderStatus next, String reason, String actor, String payload) {
        history.add(new StatusChange(UUID.randomUUID(), id, this.status, next,
                reason, actor, payload, Instant.now()));
        this.status = next;
        this.updatedAt = Instant.now();
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public ServiceCode getServiceCode() { return serviceCode; }
    public UUID getCustomerId() { return customerId; }
    public UUID getPropertyId() { return propertyId; }
    public UUID getOperationId() { return operationId; }
    public UUID getBankProductId() { return bankProductId; }
    public String getInputs() { return inputs; }
    public void setInputs(String inputs) { this.inputs = inputs; this.updatedAt = Instant.now(); }
    public BigDecimal getPriceQuoted() { return priceQuoted; }
    public BigDecimal getPriceFinal() { return priceFinal; }
    public String getCurrency() { return currency; }
    public OrderStatus getStatus() { return status; }
    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public UUID getPartnerId() { return partnerId; }
    public Instant getSlaDueAt() { return slaDueAt; }
    public List<Deliverable> getDeliverables() { return List.copyOf(deliverables); }
    public List<Payment> getPayments() { return List.copyOf(payments); }
    public List<StatusChange> getHistory() { return List.copyOf(history); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public long getVersion() { return version; }
}
