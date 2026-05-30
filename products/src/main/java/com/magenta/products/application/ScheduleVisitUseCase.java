package com.magenta.products.application;

import com.magenta.products.domain.model.Visit;
import com.magenta.products.domain.model.VisitMode;
import com.magenta.products.domain.model.VisitStatus;
import com.magenta.products.domain.port.out.DomainEventPublisher;
import com.magenta.products.domain.port.out.VisitRepository;
import com.magenta.products.domain.event.VisitRequested;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-P7: Agendar visita.
 */
@Service
@Transactional
public class ScheduleVisitUseCase {

    private final VisitRepository visitRepository;
    private final DomainEventPublisher eventPublisher;

    public ScheduleVisitUseCase(VisitRepository visitRepository, DomainEventPublisher eventPublisher) {
        this.visitRepository = visitRepository;
        this.eventPublisher = eventPublisher;
    }

    public Visit execute(Command cmd) {
        if (!cmd.slotEnd().isAfter(cmd.slotStart())) {
            throw new IllegalArgumentException("slotEnd must be after slotStart");
        }
        Instant now = Instant.now();
        Visit visit = new Visit(
                UUID.randomUUID(),
                cmd.propertyId(),
                cmd.customerId(),
                cmd.agentId(),
                cmd.slotStart(),
                cmd.slotEnd(),
                cmd.mode(),
                VisitStatus.REQUESTED,
                null, now);

        Visit saved = visitRepository.save(visit);
        eventPublisher.publish(new VisitRequested(
                saved.id(), saved.propertyId(), saved.customerId(),
                saved.agentId(), saved.slotStart(), now));
        return saved;
    }

    public record Command(
            UUID propertyId,
            UUID customerId,
            String agentId,
            Instant slotStart,
            Instant slotEnd,
            VisitMode mode) {}
}
