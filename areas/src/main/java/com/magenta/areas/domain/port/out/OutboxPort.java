package com.magenta.areas.domain.port.out;

import com.magenta.areas.domain.event.DomainEvent;

public interface OutboxPort {

    void publish(DomainEvent event);
}
