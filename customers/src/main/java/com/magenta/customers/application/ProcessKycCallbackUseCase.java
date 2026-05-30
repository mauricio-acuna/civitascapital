package com.magenta.customers.application;

import com.magenta.customers.domain.event.*;
import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.out.*;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * UC-C6 (parte 2): Procesar callback firmado del proveedor KYC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessKycCallbackUseCase {

    private final CustomerRepository customerRepository;
    private final KycProviderPort kycProvider;
    private final EventPublisher eventPublisher;

    public record Command(
            UUID customerId,
            byte[] rawPayload,
            String signature,
            long callbackTimestamp,
            String providerRef,
            boolean documentAuthentic,
            boolean livenessOk,
            boolean sanctionsClean,
            boolean pepFlag,
            boolean addressVerified,
            Integer score,
            Instant expiresAt
    ) {}

    @Transactional
    public Customer execute(Command cmd) {
        // 1. Verificar firma HMAC + replay-protection
        kycProvider.verifyCallbackSignature(cmd.rawPayload(), cmd.signature(), cmd.callbackTimestamp());

        // 2. Cargar cliente
        Customer customer = customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        // 3. Determinar resultado
        boolean approved = cmd.documentAuthentic() && cmd.livenessOk()
                && cmd.sanctionsClean() && !cmd.pepFlag();
        KycStatus newStatus = approved ? KycStatus.VERIFIED : KycStatus.REJECTED;

        Map<String, Boolean> checks = Map.of(
                "documentAuthentic", cmd.documentAuthentic(),
                "livenessOk", cmd.livenessOk(),
                "sanctionsClean", cmd.sanctionsClean(),
                "pepFlag", cmd.pepFlag(),
                "addressVerified", cmd.addressVerified()
        );

        KycState newKyc = KycState.builder()
                .status(newStatus)
                .provider(KycProvider.IDNOW)
                .checks(checks)
                .score(cmd.score())
                .verifiedAt(approved ? Instant.now() : null)
                .expiresAt(cmd.expiresAt())
                .providerRef(cmd.providerRef())
                .build();

        Customer updated = customer.withKyc(newKyc)
                .withStatus(approved ? CustomerStatus.ACTIVE : CustomerStatus.SUSPENDED)
                .withUpdatedAt(Instant.now());
        Customer saved = customerRepository.save(updated);

        // 4. Publicar evento
        DomainEvent event = approved
                ? KycVerified.builder()
                        .eventId(UuidCreator.getTimeOrderedEpoch())
                        .occurredAt(Instant.now())
                        .aggregateId(cmd.customerId())
                        .tenantId(saved.getTenantId())
                        .kycScore(cmd.score())
                        .expiresAt(cmd.expiresAt())
                        .build()
                : KycRejected.builder()
                        .eventId(UuidCreator.getTimeOrderedEpoch())
                        .occurredAt(Instant.now())
                        .aggregateId(cmd.customerId())
                        .tenantId(saved.getTenantId())
                        .reason("KYC checks failed")
                        .build();

        eventPublisher.publish(event);
        log.info("KYC callback processed customer={} status={}", cmd.customerId(), newStatus);
        return saved;
    }
}
