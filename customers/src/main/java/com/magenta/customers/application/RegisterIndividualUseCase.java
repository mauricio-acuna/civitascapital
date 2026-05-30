package com.magenta.customers.application;

import com.magenta.customers.domain.event.CustomerCreated;
import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.out.*;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-C1: Registro de cliente persona física con verificación email/SMS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterIndividualUseCase {

    private final CustomerRepository customerRepository;
    private final PiiCryptoPort piiCrypto;
    private final KeycloakPort keycloak;
    private final EventPublisher eventPublisher;

    public record Command(
            UUID tenantId,
            String nif,
            String firstName,
            String lastName,
            java.time.LocalDate birthDate,
            String nationality,
            String residenceCountry,
            String taxResidence,
            String civilStatus,
            String phone,
            String email,
            PostalAddress address,
            ProfessionalProfile professional,
            String requestedBy
    ) {}

    @Transactional
    public Customer execute(Command cmd) {
        // 1. Unicidad por NIF y email
        String nifHash = piiCrypto.hmac(cmd.nif());
        String emailHash = cmd.email() != null ? piiCrypto.hmac(cmd.email()) : null;

        if (customerRepository.existsByNifHash(nifHash)) {
            throw new DuplicateCustomerException("NIF already registered");
        }
        if (emailHash != null && customerRepository.existsByEmailHash(emailHash)) {
            throw new DuplicateCustomerException("Email already registered");
        }

        // 2. Crear usuario en Keycloak
        String keycloakId = keycloak.createUser(
                cmd.email(), cmd.firstName(), cmd.lastName(),
                null, cmd.tenantId());  // customerId se establece después del save

        // 3. Construir aggregate
        UUID customerId = UuidCreator.getTimeOrderedEpoch();
        IndividualProfile profile = IndividualProfile.builder()
                .nif(cmd.nif())
                .firstName(cmd.firstName())
                .lastName(cmd.lastName())
                .birthDate(cmd.birthDate())
                .nationality(cmd.nationality() != null ? cmd.nationality() : "ES")
                .residenceCountry(cmd.residenceCountry() != null ? cmd.residenceCountry() : "ES")
                .taxResidence(cmd.taxResidence() != null ? cmd.taxResidence() : "ES")
                .civilStatus(cmd.civilStatus())
                .phone(cmd.phone())
                .email(cmd.email())
                .address(cmd.address())
                .professional(cmd.professional())
                .build();

        Customer customer = Customer.builder()
                .id(customerId)
                .tenantId(cmd.tenantId())
                .type(CustomerType.INDIVIDUAL)
                .displayName(cmd.firstName() + " " + cmd.lastName())
                .status(CustomerStatus.DRAFT)
                .keycloakUserId(keycloakId)
                .individual(profile)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(cmd.requestedBy())
                .updatedBy(cmd.requestedBy())
                .version(0)
                .kyc(KycState.builder()
                        .status(KycStatus.PENDING)
                        .provider(KycProvider.IDNOW)
                        .checks(java.util.Map.of())
                        .build())
                .build();

        customer.validateInvariants();
        Customer saved = customerRepository.save(customer);

        // 4. Publicar evento en outbox
        eventPublisher.publish(CustomerCreated.builder()
                .eventId(UuidCreator.getTimeOrderedEpoch())
                .occurredAt(Instant.now())
                .aggregateId(saved.getId())
                .tenantId(saved.getTenantId())
                .customerType(CustomerType.INDIVIDUAL)
                .displayName(saved.getDisplayName())
                .createdBy(cmd.requestedBy())
                .build());

        log.info("Individual customer registered id={} tenant={}", saved.getId(), cmd.tenantId());
        return saved;
    }
}
