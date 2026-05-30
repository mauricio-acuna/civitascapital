package com.magenta.servicios.infrastructure.adapter.out.persistence;

import com.magenta.servicios.domain.model.*;
import com.magenta.servicios.domain.port.out.ServiceCatalogRepository;
import com.magenta.servicios.domain.port.out.ServiceOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class ServiceOrderRepositoryAdapter implements ServiceOrderRepository {

    private final ServiceOrderJpaRepository jpa;

    public ServiceOrderRepositoryAdapter(ServiceOrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ServiceOrder save(ServiceOrder order) {
        ServiceOrderJpaEntity entity = toEntity(order);
        ServiceOrderJpaEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ServiceOrder> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ServiceOrder> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpa.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Page<ServiceOrder> findByCustomerIdAndTenantId(UUID customerId, UUID tenantId, Pageable pageable) {
        return jpa.findByCustomerIdAndTenantId(customerId, tenantId, pageable).map(this::toDomain);
    }

    @Override
    public Page<ServiceOrder> findByStatusAndTenantId(OrderStatus status, UUID tenantId, Pageable pageable) {
        return jpa.findByStatusAndTenantId(status.name(), tenantId, pageable).map(this::toDomain);
    }

    @Override
    public List<ServiceOrder> findOverdueSlaOrders(Instant now) {
        return jpa.findOverdueSla(now).stream().map(this::toDomain).toList();
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private ServiceOrderJpaEntity toEntity(ServiceOrder o) {
        ServiceOrderJpaEntity e = new ServiceOrderJpaEntity();
        e.setId(o.getId());
        e.setTenantId(o.getTenantId());
        e.setServiceCode(o.getServiceCode().name());
        e.setCustomerId(o.getCustomerId());
        e.setPropertyId(o.getPropertyId());
        e.setOperationId(o.getOperationId());
        e.setBankProductId(o.getBankProductId());
        e.setInputs(o.getInputs());
        e.setPriceQuoted(o.getPriceQuoted());
        e.setPriceFinal(o.getPriceFinal());
        e.setCurrency(o.getCurrency());
        e.setStatus(o.getStatus().name());
        e.setWorkflowInstanceId(o.getWorkflowInstanceId());
        e.setPartnerId(o.getPartnerId());
        e.setSlaDueAt(o.getSlaDueAt());
        e.setCreatedAt(o.getCreatedAt());
        e.setUpdatedAt(o.getUpdatedAt());
        e.setCompletedAt(o.getCompletedAt());
        e.setVersion(o.getVersion());
        return e;
    }

    private ServiceOrder toDomain(ServiceOrderJpaEntity e) {
        return ServiceOrder.reconstitute(
                e.getId(), e.getTenantId(),
                ServiceCode.valueOf(e.getServiceCode()),
                e.getCustomerId(), e.getPropertyId(), e.getOperationId(), e.getBankProductId(),
                e.getInputs(), e.getPriceQuoted(), e.getPriceFinal(), e.getCurrency(),
                OrderStatus.valueOf(e.getStatus()),
                e.getWorkflowInstanceId(), e.getPartnerId(), e.getSlaDueAt(),
                List.of(), List.of(), List.of(),   // child collections loaded separately if needed
                e.getCreatedAt(), e.getUpdatedAt(), e.getCompletedAt(), e.getVersion());
    }
}
