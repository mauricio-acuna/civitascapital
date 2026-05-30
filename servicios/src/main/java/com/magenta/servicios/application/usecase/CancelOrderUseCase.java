package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.event.OrderCancelledEvent;
import com.magenta.servicios.domain.model.ServiceOrder;
import com.magenta.servicios.domain.port.out.OutboxEventPublisher;
import com.magenta.servicios.domain.port.out.ServiceOrderRepository;
import com.magenta.servicios.domain.port.out.WorkflowPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CancelOrderUseCase {

    private final ServiceOrderRepository orders;
    private final WorkflowPort workflow;
    private final OutboxEventPublisher outbox;

    public CancelOrderUseCase(ServiceOrderRepository orders, WorkflowPort workflow,
                               OutboxEventPublisher outbox) {
        this.orders = orders;
        this.workflow = workflow;
        this.outbox = outbox;
    }

    public ServiceOrder execute(UUID orderId, UUID tenantId, String reason, String actor) {
        ServiceOrder order = orders.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        if (order.getWorkflowInstanceId() != null) {
            workflow.cancelProcess(order.getWorkflowInstanceId(), reason);
        }

        order.cancel(reason, actor);
        ServiceOrder saved = orders.save(order);

        outbox.publish("magenta.servicios.workflow.v1",
                new OrderCancelledEvent(orderId, tenantId, reason));

        return saved;
    }

    public ServiceOrder updateInputs(UUID orderId, UUID tenantId, String newInputs, String actor) {
        ServiceOrder order = orders.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        if (order.getStatus() != com.magenta.servicios.domain.model.OrderStatus.DRAFT
                && order.getStatus() != com.magenta.servicios.domain.model.OrderStatus.QUOTED) {
            throw new IllegalStateException("Solo se pueden actualizar inputs en DRAFT o QUOTED");
        }

        order.setInputs(newInputs);
        return orders.save(order);
    }
}
