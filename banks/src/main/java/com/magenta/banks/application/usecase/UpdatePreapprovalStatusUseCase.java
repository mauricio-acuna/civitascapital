package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.event.*;
import com.magenta.banks.domain.model.PreapprovalStatus;
import com.magenta.banks.domain.model.preapproval.Preapproval;
import com.magenta.banks.domain.port.out.DomainEventPublisher;
import com.magenta.banks.domain.port.out.PreapprovalRepository;
import com.magenta.banks.domain.service.PreapprovalStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * UC-B5 (parte 2): Actualizar estado de una pre-aprobación (BANK_OFFICER / SYSTEM).
 */
@Service
public class UpdatePreapprovalStatusUseCase {

    private final PreapprovalRepository    preapprovalRepository;
    private final DomainEventPublisher     eventPublisher;
    private final PreapprovalStateMachine  stateMachine = new PreapprovalStateMachine();

    public UpdatePreapprovalStatusUseCase(PreapprovalRepository preapprovalRepository,
                                          DomainEventPublisher eventPublisher) {
        this.preapprovalRepository = preapprovalRepository;
        this.eventPublisher        = eventPublisher;
    }

    public record Command(
        UUID preapprovalId,
        PreapprovalStatus newStatus,
        String reason,
        String actor,
        List<String> conditions
    ) {}

    @Transactional
    public Preapproval execute(Command cmd) {
        Preapproval current = preapprovalRepository.findById(cmd.preapprovalId())
                .orElseThrow(() -> new NoSuchElementException("Preapproval not found: " + cmd.preapprovalId()));

        stateMachine.assertTransitionAllowed(current.status(), cmd.newStatus());

        Preapproval updated = current.transition(cmd.newStatus(), cmd.reason(), cmd.actor());
        Preapproval saved   = preapprovalRepository.save(updated);

        BanksDomainEvent event = switch (cmd.newStatus()) {
            case APPROVED -> new PreapprovalApproved(
                    saved.tenantId(), saved.id(), saved.customerId());
            case REJECTED -> new PreapprovalRejected(
                    saved.tenantId(), saved.id(), saved.customerId(), cmd.reason());
            case EXPIRED  -> new PreapprovalExpired(
                    saved.tenantId(), saved.id(), saved.customerId());
            default -> null;
        };
        if (event != null) eventPublisher.publish(event);

        return saved;
    }
}
