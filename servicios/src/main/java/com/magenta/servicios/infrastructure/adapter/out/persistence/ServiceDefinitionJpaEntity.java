package com.magenta.servicios.infrastructure.adapter.out.persistence;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_definitions", schema = "services")
public class ServiceDefinitionJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "pricing_model", nullable = false, length = 24)
    private String pricingModel;

    @Column(name = "base_price", precision = 14, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "price_formula", columnDefinition = "TEXT")
    private String priceFormula;

    @Column(name = "sla_hours", nullable = false)
    private int slaHours;

    @Column(name = "workflow_key", nullable = false, length = 80)
    private String workflowKey;

    @Column(name = "inputs_schema", columnDefinition = "JSONB")
    private String inputsSchema;

    @Column(name = "outputs_schema", columnDefinition = "JSONB")
    private String outputsSchema;

    @Column(name = "requires_kyc", nullable = false)
    private boolean requiresKyc;

    @Type(ListArrayType.class)
    @Column(name = "valid_for", columnDefinition = "TEXT[]")
    private List<String> validFor;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    // Getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPricingModel() { return pricingModel; }
    public void setPricingModel(String pricingModel) { this.pricingModel = pricingModel; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public String getPriceFormula() { return priceFormula; }
    public void setPriceFormula(String priceFormula) { this.priceFormula = priceFormula; }
    public int getSlaHours() { return slaHours; }
    public void setSlaHours(int slaHours) { this.slaHours = slaHours; }
    public String getWorkflowKey() { return workflowKey; }
    public void setWorkflowKey(String workflowKey) { this.workflowKey = workflowKey; }
    public String getInputsSchema() { return inputsSchema; }
    public void setInputsSchema(String inputsSchema) { this.inputsSchema = inputsSchema; }
    public String getOutputsSchema() { return outputsSchema; }
    public void setOutputsSchema(String outputsSchema) { this.outputsSchema = outputsSchema; }
    public boolean isRequiresKyc() { return requiresKyc; }
    public void setRequiresKyc(boolean requiresKyc) { this.requiresKyc = requiresKyc; }
    public List<String> getValidFor() { return validFor; }
    public void setValidFor(List<String> validFor) { this.validFor = validFor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
