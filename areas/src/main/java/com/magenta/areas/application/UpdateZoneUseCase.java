package com.magenta.areas.application;

import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.port.in.UpdateZonePort;
import com.magenta.areas.domain.port.out.OutboxPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateZoneUseCase implements UpdateZonePort {

    private final ZoneRepositoryPort repository;
    private final OutboxPort outbox;

    public UpdateZoneUseCase(ZoneRepositoryPort repository, OutboxPort outbox) {
        this.repository = repository;
        this.outbox     = outbox;
    }

    @Override
    public Zone execute(Command command) {
        Zone zone = repository.findById(command.id())
                .orElseThrow(() -> new ZoneNotFoundException(command.id()));

        zone.update(command.name(), command.postalCodes(), command.tags(),
                command.population(), command.areaKm2(), command.actorId());

        Zone saved = repository.save(zone);
        saved.pullDomainEvents().forEach(outbox::publish);
        return saved;
    }
}
