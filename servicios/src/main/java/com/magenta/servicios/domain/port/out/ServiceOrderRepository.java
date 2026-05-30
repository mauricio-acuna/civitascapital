package com.magenta.servicios.domain.port.out;

import com.magenta.servicios.domain.model.OrderStatus;
import com.magenta.servicios.domain.model.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository {
    ServiceOrder save(ServiceOrder order);
    Optional<ServiceOrder> findById(UUID id);
    Optional<ServiceOrder> findByIdAndTenantId(UUID id, UUID tenantId);
    Page<ServiceOrder> findByCustomerIdAndTenantId(UUID customerId, UUID tenantId, Pageable pageable);
    Page<ServiceOrder> findByStatusAndTenantId(OrderStatus status, UUID tenantId, Pageable pageable);
    List<ServiceOrder> findOverdueSlaOrders(Instant now);
}
