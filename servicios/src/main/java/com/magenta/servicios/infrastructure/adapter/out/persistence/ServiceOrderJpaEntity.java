package com.magenta.servicios.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_orders", schema = "services")
public class ServiceOrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "service_code", nullable = false, length = 40)
    private String serviceCode;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "property_id")
    private UUID propertyId;

    @Column(name = "operation_id")
    private UUID operationId;

    @Column(name = "bank_product_id")
    private UUID bankProductId;

    @Column(nullable = false, columnDefinition = "JSONB")
    private String inputs;

    @Column(name = "price_quoted", nullable = false, precision = 14, scale = 2)
    private BigDecimal priceQuoted;

    @Column(name = "price_final", precision = 14, scale = 2)
    private BigDecimal priceFinal;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "workflow_instance_id", length = 80)
    private String workflowInstanceId;

    @Column(name = "partner_id")
    private UUID partnerId;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private long version;

    // Getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public UUID getOperationId() { return operationId; }
    public void setOperationId(UUID operationId) { this.operationId = operationId; }
    public UUID getBankProductId() { return bankProductId; }
    public void setBankProductId(UUID bankProductId) { this.bankProductId = bankProductId; }
    public String getInputs() { return inputs; }
    public void setInputs(String inputs) { this.inputs = inputs; }
    public BigDecimal getPriceQuoted() { return priceQuoted; }
    public void setPriceQuoted(BigDecimal priceQuoted) { this.priceQuoted = priceQuoted; }
    public BigDecimal getPriceFinal() { return priceFinal; }
    public void setPriceFinal(BigDecimal priceFinal) { this.priceFinal = priceFinal; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }
    public UUID getPartnerId() { return partnerId; }
    public void setPartnerId(UUID partnerId) { this.partnerId = partnerId; }
    public Instant getSlaDueAt() { return slaDueAt; }
    public void setSlaDueAt(Instant slaDueAt) { this.slaDueAt = slaDueAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
