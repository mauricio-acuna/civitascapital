package com.magenta.servicios.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate root: definición de un servicio del catálogo.
 * Inmutable después de creación (actualizaciones vía Flyway seed o admin API).
 */
public class ServiceDefinition {

    private final UUID id;
    private final ServiceCode code;
    private final String name;
    private final String description;
    private final String category;
    private final PricingModel pricingModel;
    private final BigDecimal basePrice;
    private final String priceFormula;
    private final int slaHours;
    private final List<String> partnerRefs;
    private final String workflowKey;
    private final String inputsSchema;
    private final String outputsSchema;
    private final boolean requiresKyc;
    private final ServiceDefinitionStatus status;
    private final Set<String> validFor;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final long version;

    public ServiceDefinition(UUID id, ServiceCode code, String name, String description,
                             String category, PricingModel pricingModel, BigDecimal basePrice,
                             String priceFormula, int slaHours, List<String> partnerRefs,
                             String workflowKey, String inputsSchema, String outputsSchema,
                             boolean requiresKyc, ServiceDefinitionStatus status,
                             Set<String> validFor, Instant createdAt, Instant updatedAt,
                             long version) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
        this.pricingModel = pricingModel;
        this.basePrice = basePrice;
        this.priceFormula = priceFormula;
        this.slaHours = slaHours;
        this.partnerRefs = partnerRefs != null ? List.copyOf(partnerRefs) : List.of();
        this.workflowKey = workflowKey;
        this.inputsSchema = inputsSchema;
        this.outputsSchema = outputsSchema;
        this.requiresKyc = requiresKyc;
        this.status = status;
        this.validFor = validFor != null ? Set.copyOf(validFor) : Set.of();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public boolean isActive() {
        return ServiceDefinitionStatus.ACTIVE.equals(status);
    }

    public UUID getId() { return id; }
    public ServiceCode getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public PricingModel getPricingModel() { return pricingModel; }
    public BigDecimal getBasePrice() { return basePrice; }
    public String getPriceFormula() { return priceFormula; }
    public int getSlaHours() { return slaHours; }
    public List<String> getPartnerRefs() { return partnerRefs; }
    public String getWorkflowKey() { return workflowKey; }
    public String getInputsSchema() { return inputsSchema; }
    public String getOutputsSchema() { return outputsSchema; }
    public boolean isRequiresKyc() { return requiresKyc; }
    public ServiceDefinitionStatus getStatus() { return status; }
    public Set<String> getValidFor() { return validFor; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
