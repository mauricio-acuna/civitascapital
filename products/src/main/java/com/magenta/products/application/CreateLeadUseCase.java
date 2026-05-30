package com.magenta.products.application;

import com.magenta.products.domain.model.Lead;
import com.magenta.products.domain.model.LeadSource;
import com.magenta.products.domain.model.LeadStatus;
import com.magenta.products.domain.port.out.DomainEventPublisher;
import com.magenta.products.domain.port.out.LeadRepository;
import com.magenta.products.domain.event.LeadCreated;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-P6: Abrir lead.
 */
@Service
@Transactional
public class CreateLeadUseCase {

    private final LeadRepository leadRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateLeadUseCase(LeadRepository leadRepository, DomainEventPublisher eventPublisher) {
        this.leadRepository = leadRepository;
        this.eventPublisher = eventPublisher;
    }

    public Lead execute(Command cmd) {
        Instant now = Instant.now();
        Lead lead = new Lead(
                UUID.randomUUID(),
                cmd.propertyId(),
                cmd.operationId(),
                cmd.customerId(),
                cmd.anonContact(),
                cmd.source(),
                cmd.message(),
                LeadStatus.NEW,
                null,
                now, now);

        Lead saved = leadRepository.save(lead);
        eventPublisher.publish(new LeadCreated(saved.id(), saved.propertyId(), null,
                saved.source().name(), now));
        return saved;
    }

    public record Command(
            UUID propertyId,
            UUID operationId,
            UUID customerId,
            Lead.AnonContact anonContact,
            LeadSource source,
            String message) {}
}
