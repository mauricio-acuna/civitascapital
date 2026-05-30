package com.magenta.servicios.domain.port.out;

/**
 * Puerto de salida para persistir eventos en el outbox y publicarlos transaccionalmente.
 */
public interface OutboxEventPublisher {
    void publish(String topic, Object event);
}
