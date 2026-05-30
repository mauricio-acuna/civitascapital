package com.magenta.areas.application;

import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.port.in.DeleteZonePort;
import com.magenta.areas.domain.port.out.OutboxPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteZoneUseCase implements DeleteZonePort {

    private final ZoneRepositoryPort repository;
    private final OutboxPort outbox;

    public DeleteZoneUseCase(ZoneRepositoryPort repository, OutboxPort outbox) {
        this.repository = repository;
        this.outbox     = outbox;
    }

    @Override
    public void execute(UUID id, String actorId) {
        Zone zone = repository.findById(id)
                .orElseThrow(() -> new ZoneNotFoundException(id));
        zone.deprecate(actorId);
        Zone saved = repository.save(zone);
        saved.pullDomainEvents().forEach(outbox::publish);
    }
}
