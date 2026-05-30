package com.magenta.areas.application;

import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.model.ZoneType;
import com.magenta.areas.domain.port.in.CreateZonePort;
import com.magenta.areas.domain.port.out.OutboxPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateZoneUseCase implements CreateZonePort {

    private final ZoneRepositoryPort repository;
    private final OutboxPort outbox;

    public CreateZoneUseCase(ZoneRepositoryPort repository, OutboxPort outbox) {
        this.repository = repository;
        this.outbox     = outbox;
    }

    @Override
    public Zone execute(Command command) {
        validateParentType(command);

        Zone zone = Zone.create(command.tenantId(), command.code(), command.name(),
                command.type(), command.parentId(), command.centroid(), command.actorId());

        Zone saved = repository.save(zone);
        saved.pullDomainEvents().forEach(outbox::publish);
        return saved;
    }

    private void validateParentType(Command command) {
        if (command.parentId() == null) {
            if (command.type() != ZoneType.COUNTRY) {
                throw new IllegalArgumentException("Only COUNTRY zones can have no parent");
            }
            return;
        }
        Zone parent = repository.findById(command.parentId())
                .orElseThrow(() -> new ZoneNotFoundException(command.parentId()));
        ZoneType expectedParentType = command.type().parentType();
        if (expectedParentType != null && parent.getType() != expectedParentType) {
            throw new IllegalArgumentException(
                    "Parent type must be " + expectedParentType + " for zone type " + command.type());
        }
    }
}
