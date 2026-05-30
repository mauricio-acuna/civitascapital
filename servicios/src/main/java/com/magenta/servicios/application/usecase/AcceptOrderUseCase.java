package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.event.OrderAcceptedEvent;
import com.magenta.servicios.domain.event.OrderInProgressEvent;
import com.magenta.servicios.domain.model.*;
import com.magenta.servicios.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AcceptOrderUseCase {

    private final ServiceOrderRepository orders;
    private final ServiceCatalogRepository catalog;
    private final WorkflowPort workflow;
    private final OutboxEventPublisher outbox;

    public AcceptOrderUseCase(ServiceOrderRepository orders,
                              ServiceCatalogRepository catalog,
                              WorkflowPort workflow,
                              OutboxEventPublisher outbox) {
        this.orders = orders;
        this.catalog = catalog;
        this.workflow = workflow;
        this.outbox = outbox;
    }

    public ServiceOrder execute(UUID orderId, UUID tenantId, String actor) {
        ServiceOrder order = orders.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        order.accept(actor);
        outbox.publish("magenta.servicios.workflow.v1",
                new OrderAcceptedEvent(orderId, tenantId, order.getCustomerId()));

        // Obtener workflowKey del catálogo
        ServiceDefinition def = catalog.findByCode(order.getServiceCode())
                .orElseThrow(() -> new IllegalStateException("Definición de servicio no encontrada"));

        String instanceId = workflow.startProcess(def.getWorkflowKey(), orderId,
                Map.of("orderId", orderId.toString(),
                        "tenantId", tenantId.toString(),
                        "serviceCode", order.getServiceCode().name(),
                        "customerId", order.getCustomerId().toString()));

        order.startWorkflow(instanceId, "SYSTEM");

        ServiceOrder saved = orders.save(order);

        outbox.publish("magenta.servicios.workflow.v1",
                new OrderInProgressEvent(orderId, tenantId, instanceId));

        return saved;
    }
}
