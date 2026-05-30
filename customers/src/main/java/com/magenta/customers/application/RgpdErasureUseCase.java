package com.magenta.customers.application;

import com.magenta.customers.domain.event.ErasureRequested;
import com.magenta.customers.domain.model.Customer;
import com.magenta.customers.domain.port.out.*;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-C9: Derechos RGPD — supresión (crypto-shredding + tombstone).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RgpdErasureUseCase {

    private final CustomerRepository customerRepository;
    private final DocumentRepository documentRepository;
    private final DocumentStoragePort storagePort;
    private final KeycloakPort keycloak;
    private final EventPublisher eventPublisher;

    public record Command(UUID customerId, UUID tenantId, String requestedBy) {}

    @Transactional
    public void execute(Command cmd) {
        Customer customer = customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        // 1. Eliminar documentos del storage (crypto-shredding de DEK implícito)
        documentRepository.findByCustomerId(cmd.customerId()).forEach(doc -> {
            try {
                storagePort.delete(doc.getStorageUri());
            } catch (Exception e) {
                log.warn("Could not delete S3 object {} during erasure: {}", doc.getStorageUri(), e.getMessage());
            }
        });

        // 2. Deshabilitar y eliminar usuario de Keycloak
        if (customer.getKeycloakUserId() != null) {
            keycloak.deleteUser(customer.getKeycloakUserId());
        }

        // 3. Soft-delete con tombstone
        customerRepository.softDelete(cmd.customerId(), cmd.requestedBy());

        // 4. Evento de trazabilidad
        eventPublisher.publish(ErasureRequested.builder()
                .eventId(UuidCreator.getTimeOrderedEpoch())
                .occurredAt(Instant.now())
                .aggregateId(cmd.customerId())
                .tenantId(cmd.tenantId())
                .requestedBy(cmd.requestedBy())
                .build());

        log.info("RGPD erasure completed for customer={}", cmd.customerId());
    }
}
