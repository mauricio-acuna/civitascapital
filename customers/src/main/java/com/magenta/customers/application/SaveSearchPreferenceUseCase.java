package com.magenta.customers.application;

import com.magenta.customers.domain.event.SearchPreferenceCreated;
import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.out.*;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * UC-C7: Guardar búsqueda guardada con alertas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaveSearchPreferenceUseCase {

    private final CustomerRepository customerRepository;
    private final SearchPreferenceRepository preferenceRepository;
    private final EventPublisher eventPublisher;

    public record Command(
            UUID customerId,
            UUID tenantId,
            OperationType operationType,
            Set<String> propertyTypes,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Integer surfaceMin,
            Short roomsMin,
            Short bathroomsMin,
            Set<UUID> zoneIds,
            boolean requiresFiber,
            Integer maxRiskOccupation,
            AlertChannel alertChannel,
            AlertFrequency alertFrequency,
            String requestedBy
    ) {}

    @Transactional
    public SearchPreference execute(Command cmd) {
        customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        SearchPreference pref = SearchPreference.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .customerId(cmd.customerId())
                .operationType(cmd.operationType())
                .propertyTypes(cmd.propertyTypes() != null ? cmd.propertyTypes() : Set.of())
                .priceMin(cmd.priceMin())
                .priceMax(cmd.priceMax())
                .surfaceMin(cmd.surfaceMin())
                .roomsMin(cmd.roomsMin())
                .bathroomsMin(cmd.bathroomsMin())
                .zoneIds(cmd.zoneIds() != null ? cmd.zoneIds() : Set.of())
                .requiresFiber(cmd.requiresFiber())
                .maxRiskOccupation(cmd.maxRiskOccupation())
                .alertChannel(cmd.alertChannel() != null ? cmd.alertChannel() : AlertChannel.NONE)
                .alertFrequency(cmd.alertFrequency() != null ? cmd.alertFrequency() : AlertFrequency.WEEKLY)
                .active(true)
                .createdAt(Instant.now())
                .build();

        SearchPreference saved = preferenceRepository.save(pref);

        eventPublisher.publish(SearchPreferenceCreated.builder()
                .eventId(UuidCreator.getTimeOrderedEpoch())
                .occurredAt(Instant.now())
                .aggregateId(cmd.customerId())
                .tenantId(cmd.tenantId())
                .preferenceId(saved.getId())
                .build());

        return saved;
    }
}
