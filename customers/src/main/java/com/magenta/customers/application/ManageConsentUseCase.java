package com.magenta.customers.application;

import com.magenta.customers.domain.event.ConsentGranted;
import com.magenta.customers.domain.event.ConsentRevoked;
import com.magenta.customers.domain.model.Consent;
import com.magenta.customers.domain.port.out.ConsentRepository;
import com.magenta.customers.domain.port.out.CustomerRepository;
import com.magenta.customers.domain.port.out.EventPublisher;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * UC-C9 (parte consentimientos): Gestión RGPD de consentimientos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManageConsentUseCase {

    private final CustomerRepository customerRepository;
    private final ConsentRepository consentRepository;
    private final EventPublisher eventPublisher;

    public record GrantCommand(
            UUID customerId,
            UUID tenantId,
            String purpose,
            String legalBasis,
            Map<String, Object> evidence,
            String requestedBy
    ) {}

    public record RevokeCommand(
            UUID customerId,
            UUID tenantId,
            String purpose,
            String requestedBy
    ) {}

    @Transactional
    public Consent grant(GrantCommand cmd) {
        customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        Consent consent = Consent.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .customerId(cmd.customerId())
                .purpose(cmd.purpose())
                .granted(true)
                .grantedAt(Instant.now())
                .legalBasis(cmd.legalBasis())
                .evidence(cmd.evidence())
                .build();

        Consent saved = consentRepository.save(consent);
        eventPublisher.publish(ConsentGranted.builder()
                .eventId(UuidCreator.getTimeOrderedEpoch())
                .occurredAt(Instant.now())
                .aggregateId(cmd.customerId())
                .tenantId(cmd.tenantId())
                .purpose(cmd.purpose())
                .build());
        return saved;
    }

    @Transactional
    public Consent revoke(RevokeCommand cmd) {
        Consent existing = consentRepository.findByCustomerIdAndPurpose(cmd.customerId(), cmd.purpose())
                .orElseThrow(() -> new IllegalArgumentException("Consent not found for purpose: " + cmd.purpose()));

        Consent revoked = existing.withGranted(false).withRevokedAt(Instant.now());
        Consent saved = consentRepository.save(revoked);

        eventPublisher.publish(ConsentRevoked.builder()
                .eventId(UuidCreator.getTimeOrderedEpoch())
                .occurredAt(Instant.now())
                .aggregateId(cmd.customerId())
                .tenantId(cmd.tenantId())
                .purpose(cmd.purpose())
                .build());
        return saved;
    }
}
