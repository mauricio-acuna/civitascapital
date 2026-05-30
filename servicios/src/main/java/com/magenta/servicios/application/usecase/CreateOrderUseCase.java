package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.event.OrderCreatedEvent;
import com.magenta.servicios.domain.model.*;
import com.magenta.servicios.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class CreateOrderUseCase {

    private final ServiceCatalogRepository catalog;
    private final ServiceOrderRepository orders;
    private final QuoteServiceUseCase quoteUseCase;
    private final OutboxEventPublisher outbox;

    public CreateOrderUseCase(ServiceCatalogRepository catalog,
                              ServiceOrderRepository orders,
                              QuoteServiceUseCase quoteUseCase,
                              OutboxEventPublisher outbox) {
        this.catalog = catalog;
        this.orders = orders;
        this.quoteUseCase = quoteUseCase;
        this.outbox = outbox;
    }

    public ServiceOrder execute(UUID tenantId, ServiceCode serviceCode, UUID customerId,
                                UUID propertyId, UUID operationId, UUID bankProductId,
                                String inputsJson) {
        ServiceDefinition def = catalog.findByCode(serviceCode)
                .filter(ServiceDefinition::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no disponible: " + serviceCode));

        QuoteServiceUseCase.QuoteResult quote = quoteUseCase.execute(
                serviceCode, customerId, propertyId, operationId, inputsJson);

        UUID orderId = UUID.randomUUID();
        ServiceOrder order = new ServiceOrder(orderId, tenantId, serviceCode, customerId,
                propertyId, operationId, bankProductId, inputsJson,
                quote.priceQuoted(), quote.currency());

        Instant slaDue = Instant.now().plus(def.getSlaHours(), ChronoUnit.HOURS);
        order.quote(quote.priceQuoted(), slaDue, "SYSTEM");

        ServiceOrder saved = orders.save(order);

        outbox.publish("magenta.servicios.workflow.v1",
                new OrderCreatedEvent(orderId, tenantId, customerId, serviceCode));

        return saved;
    }
}
