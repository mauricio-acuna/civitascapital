package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.event.AppraisalIssued;
import com.magenta.banks.domain.model.appraisal.Appraisal;
import com.magenta.banks.domain.port.out.AppraisalRepository;
import com.magenta.banks.domain.port.out.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * UC-B10: Registrar tasación y recalcular LTV.
 */
@Service
public class RegisterAppraisalUseCase {

    private final AppraisalRepository  appraisalRepository;
    private final DomainEventPublisher eventPublisher;

    public RegisterAppraisalUseCase(AppraisalRepository appraisalRepository,
                                    DomainEventPublisher eventPublisher) {
        this.appraisalRepository = appraisalRepository;
        this.eventPublisher      = eventPublisher;
    }

    public record Command(
        UUID tenantId,
        UUID propertyId,
        UUID customerId,
        UUID providerId,
        BigDecimal marketValue,
        BigDecimal mortgageValue,
        BigDecimal surfaceSqm,
        LocalDate issuedAt,
        String pdfUrl
    ) {}

    @Transactional
    public Appraisal execute(Command cmd) {
        LocalDate validUntil = cmd.issuedAt().plusMonths(6);

        Appraisal appraisal = new Appraisal(
                UUID.randomUUID(), cmd.tenantId(), cmd.propertyId(), cmd.customerId(),
                cmd.providerId(), "ECO_805_2003", cmd.marketValue(), cmd.mortgageValue(),
                cmd.surfaceSqm(), cmd.issuedAt(), validUntil, cmd.pdfUrl(),
                List.of(), Instant.now());

        Appraisal saved = appraisalRepository.save(appraisal);

        eventPublisher.publish(new AppraisalIssued(
                cmd.tenantId(), saved.id(), cmd.propertyId(),
                cmd.marketValue(), cmd.mortgageValue(), validUntil));

        return saved;
    }
}
