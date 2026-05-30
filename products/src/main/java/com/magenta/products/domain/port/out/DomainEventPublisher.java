package com.magenta.products.domain.port.out;

/**
 * Outbound port: publish domain events to the message bus (via Outbox pattern).
 */
public interface DomainEventPublisher {
    void publish(Object domainEvent);
}
