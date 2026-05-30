package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.event.BanksDomainEvent;

public interface DomainEventPublisher {
    void publish(BanksDomainEvent event);
}
