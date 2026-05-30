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
 * UC-C2: Alta de persona jurídica con verificación CIF + RegMercantil.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterLegalEntityUseCase {

    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisher;

    public record Command(
            UUID tenantId,
            String cif,
            String legalName,
            String tradeName,
            String legalForm,
            String regMercantilNumber,
            java.time.LocalDate foundedAt,
            String cnae,
            String representativeNif,
            PostalAddress address,
            java.util.List<UltimateBeneficialOwner> uboList,
            String requestedBy
    ) {}

    @Transactional
    public Customer execute(Command cmd) {
        UUID customerId = UuidCreator.getTimeOrderedEpoch();

        LegalEntityProfile profile = LegalEntityProfile.builder()
                .cif(cmd.cif())
                .legalName(cmd.legalName())
                .tradeName(cmd.tradeName())
                .legalForm(cmd.legalForm())
                .regMercantilNumber(cmd.regMercantilNumber())
                .foundedAt(cmd.foundedAt())
                .cnae(cmd.cnae())
                .representativeNif(cmd.representativeNif())
                .address(cmd.address())
                .uboList(cmd.uboList() != null ? cmd.uboList() : java.util.List.of())
                .build();

        Customer customer = Customer.builder()
                .id(customerId)
                .tenantId(cmd.tenantId())
                .type(CustomerType.LEGAL_ENTITY)
                .displayName(cmd.legalName())
                .status(CustomerStatus.DRAFT)
                .legalEntity(profile)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(cmd.requestedBy())
                .updatedBy(cmd.requestedBy())
                .version(0)
                .kyc(KycState.builder()
                        .status(KycStatus.PENDING)
                        .provider(KycProvider.MANUAL)
                        .checks(java.util.Map.of())
                        .build())
                .build();

        customer.validateInvariants();
        Customer saved = customerRepository.save(customer);

        eventPublisher.publish(CustomerCreated.builder()
                .eventId(UuidCreator.getTimeOrderedEpoch())
                .occurredAt(Instant.now())
                .aggregateId(saved.getId())
                .tenantId(saved.getTenantId())
                .customerType(CustomerType.LEGAL_ENTITY)
                .displayName(saved.getDisplayName())
                .createdBy(cmd.requestedBy())
                .build());

        log.info("Legal entity customer registered id={} cif={}", saved.getId(), cmd.cif());
        return saved;
    }
}
