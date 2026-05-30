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
import java.util.List;
import java.util.UUID;

/**
 * UC-C3: Crear unidad familiar y vincular titulares con su % de titularidad.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateHouseholdUseCase {

    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisher;

    public record Command(
            UUID tenantId,
            String displayName,
            HouseholdProfile.Relationship relationship,
            int dependentsCount,
            List<HouseholdMember> members,
            String requestedBy
    ) {}

    @Transactional
    public Customer execute(Command cmd) {
        if (cmd.members() == null || cmd.members().isEmpty()) {
            throw new IllegalArgumentException("Household must have at least one member");
        }
        // Validar que todos los individuales existan
        for (HouseholdMember m : cmd.members()) {
            customerRepository.findById(m.getIndividualId())
                    .filter(c -> c.getType() == CustomerType.INDIVIDUAL)
                    .orElseThrow(() -> new CustomerNotFoundException(m.getIndividualId()));
        }

        UUID customerId = UuidCreator.getTimeOrderedEpoch();
        HouseholdProfile profile = HouseholdProfile.builder()
                .members(cmd.members())
                .relationship(cmd.relationship())
                .dependentsCount(cmd.dependentsCount())
                .build();

        Customer customer = Customer.builder()
                .id(customerId)
                .tenantId(cmd.tenantId())
                .type(CustomerType.HOUSEHOLD)
                .displayName(cmd.displayName())
                .status(CustomerStatus.ACTIVE)
                .household(profile)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(cmd.requestedBy())
                .updatedBy(cmd.requestedBy())
                .version(0)
                .build();

        customer.validateInvariants();
        Customer saved = customerRepository.save(customer);

        eventPublisher.publish(CustomerCreated.builder()
                .eventId(UuidCreator.getTimeOrderedEpoch())
                .occurredAt(Instant.now())
                .aggregateId(saved.getId())
                .tenantId(saved.getTenantId())
                .customerType(CustomerType.HOUSEHOLD)
                .displayName(saved.getDisplayName())
                .createdBy(cmd.requestedBy())
                .build());

        return saved;
    }
}
