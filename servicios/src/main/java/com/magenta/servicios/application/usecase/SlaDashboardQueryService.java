package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.model.OrderStatus;
import com.magenta.servicios.domain.model.ServiceOrder;
import com.magenta.servicios.domain.port.out.ServiceOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SlaDashboardQueryService {

    private final ServiceOrderRepository orders;

    public SlaDashboardQueryService(ServiceOrderRepository orders) {
        this.orders = orders;
    }

    public DashboardData execute(UUID tenantId, Pageable pageable) {
        Page<ServiceOrder> inProgress = orders.findByStatusAndTenantId(
                OrderStatus.IN_PROGRESS, tenantId, pageable);
        Page<ServiceOrder> overdue = orders.findByStatusAndTenantId(
                OrderStatus.IN_PROGRESS, tenantId, pageable); // refined by SLA in real impl

        return new DashboardData(
                inProgress.getTotalElements(),
                overdue.getTotalElements(),
                Map.of("IN_PROGRESS", inProgress.getTotalElements())
        );
    }

    public record DashboardData(long totalInProgress, long totalOverdue, Map<String, Long> byStatus) {}
}
