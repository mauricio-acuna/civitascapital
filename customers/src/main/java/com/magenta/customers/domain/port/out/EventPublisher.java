package com.magenta.customers.domain.port.out;

import com.magenta.customers.domain.event.DomainEvent;

public interface EventPublisher {
    /** Persiste el evento en la tabla outbox (dentro de la misma transacción). */
    void publish(DomainEvent event);
}
