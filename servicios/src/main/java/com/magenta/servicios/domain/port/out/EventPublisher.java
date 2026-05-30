package com.magenta.servicios.domain.port.out;

public interface EventPublisher {
    void publish(String topic, Object event);
}
