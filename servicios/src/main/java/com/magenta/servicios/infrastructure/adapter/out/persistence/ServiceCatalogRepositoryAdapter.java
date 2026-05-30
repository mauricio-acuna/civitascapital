package com.magenta.servicios.infrastructure.adapter.out.persistence;

import com.magenta.servicios.domain.model.*;
import com.magenta.servicios.domain.port.out.ServiceCatalogRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ServiceCatalogRepositoryAdapter implements ServiceCatalogRepository {

    private final ServiceDefinitionJpaRepository jpa;

    public ServiceCatalogRepositoryAdapter(ServiceDefinitionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Cacheable(value = "catalog", key = "'all-active'")
    public List<ServiceDefinition> findAllActive() {
        return jpa.findAllActive().stream().map(this::toDomain).toList();
    }

    @Override
    @Cacheable(value = "catalog", key = "#code.name()")
    public Optional<ServiceDefinition> findByCode(ServiceCode code) {
        return jpa.findByCode(code.name()).map(this::toDomain);
    }

    @Override
    public void save(ServiceDefinition definition) {
        jpa.save(toEntity(definition));
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private ServiceDefinition toDomain(ServiceDefinitionJpaEntity e) {
        return new ServiceDefinition(
                e.getId(),
                ServiceCode.valueOf(e.getCode()),
                e.getName(), e.getDescription(), e.getCategory(),
                PricingModel.valueOf(e.getPricingModel()),
                e.getBasePrice(), e.getPriceFormula(), e.getSlaHours(),
                e.getValidFor() != null ? new ArrayList<>() : List.of(),
                e.getWorkflowKey(), e.getInputsSchema(), e.getOutputsSchema(),
                e.isRequiresKyc(),
                ServiceDefinitionStatus.valueOf(e.getStatus()),
                e.getValidFor() != null ? new HashSet<>(e.getValidFor()) : Set.of(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getVersion());
    }

    private ServiceDefinitionJpaEntity toEntity(ServiceDefinition d) {
        ServiceDefinitionJpaEntity e = new ServiceDefinitionJpaEntity();
        e.setId(d.getId());
        e.setCode(d.getCode().name());
        e.setName(d.getName());
        e.setDescription(d.getDescription());
        e.setCategory(d.getCategory());
        e.setPricingModel(d.getPricingModel().name());
        e.setBasePrice(d.getBasePrice());
        e.setPriceFormula(d.getPriceFormula());
        e.setSlaHours(d.getSlaHours());
        e.setWorkflowKey(d.getWorkflowKey());
        e.setInputsSchema(d.getInputsSchema());
        e.setOutputsSchema(d.getOutputsSchema());
        e.setRequiresKyc(d.isRequiresKyc());
        e.setValidFor(d.getValidFor() != null ? new ArrayList<>(d.getValidFor()) : List.of());
        e.setStatus(d.getStatus().name());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        e.setVersion(d.getVersion());
        return e;
    }
}
