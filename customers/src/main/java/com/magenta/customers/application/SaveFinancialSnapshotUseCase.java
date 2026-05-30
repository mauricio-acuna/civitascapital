package com.magenta.customers.application;

import com.magenta.customers.domain.event.FinancialSnapshotPublished;
import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.in.AffordabilityPort;
import com.magenta.customers.domain.port.out.*;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * UC-C4: Guardar perfil financiero del cliente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaveFinancialSnapshotUseCase {

    private final CustomerRepository customerRepository;
    private final FinancialSnapshotRepository snapshotRepository;
    private final DocumentRepository documentRepository;
    private final AffordabilityPort affordability;
    private final EventPublisher eventPublisher;

    public record Command(
            UUID customerId,
            UUID tenantId,
            LocalDate asOf,
            BigDecimal netIncomeMonthly,
            int payments,
            BigDecimal grossIncomeAnnual,
            BigDecimal otherDebtMonthly,
            boolean cirbeFlag,
            BigDecimal ownFunds,
            int existingProperties,
            BigDecimal rentalIncomeMonthly,
            String requestedBy
    ) {}

    @Transactional
    public FinancialSnapshot execute(Command cmd) {
        customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        // Calcular confidence según docs verificados + KYC
        BigDecimal confidence = computeConfidence(cmd.customerId());

        // Calcular affordability
        FinancialSnapshot draft = FinancialSnapshot.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .customerId(cmd.customerId())
                .asOf(cmd.asOf() != null ? cmd.asOf() : LocalDate.now())
                .netIncomeMonthly(cmd.netIncomeMonthly())
                .payments(cmd.payments() > 0 ? cmd.payments() : 12)
                .grossIncomeAnnual(cmd.grossIncomeAnnual())
                .otherDebtMonthly(cmd.otherDebtMonthly() != null ? cmd.otherDebtMonthly() : BigDecimal.ZERO)
                .cirbeFlag(cmd.cirbeFlag())
                .ownFunds(cmd.ownFunds() != null ? cmd.ownFunds() : BigDecimal.ZERO)
                .existingProperties(cmd.existingProperties())
                .rentalIncomeMonthly(cmd.rentalIncomeMonthly() != null ? cmd.rentalIncomeMonthly() : BigDecimal.ZERO)
                .confidence(confidence)
                .build();

        ComputedAffordability computed = affordability.compute(draft);
        FinancialSnapshot snapshot = draft.withComputed(computed);
        FinancialSnapshot saved = snapshotRepository.save(snapshot);

        // Calcular incomeBand (no PII)
        String incomeBand = incomeBand(cmd.netIncomeMonthly());

        eventPublisher.publish(FinancialSnapshotPublished.builder()
                .eventId(UuidCreator.getTimeOrderedEpoch())
                .occurredAt(Instant.now())
                .aggregateId(cmd.customerId())
                .tenantId(cmd.tenantId())
                .snapshotId(saved.getId())
                .asOf(saved.getAsOf())
                .incomeBand(incomeBand)
                .confidence(saved.getConfidence())
                .build());

        return saved;
    }

    private BigDecimal computeConfidence(UUID customerId) {
        long validDocs = documentRepository.countValidByCustomerId(customerId);
        // Fórmula simplificada; el cálculo completo está en ConfidenceCalculator
        double docScore = Math.min(1.0, validDocs / 5.0);
        return BigDecimal.valueOf(0.4 * docScore + 0.1);
    }

    private String incomeBand(BigDecimal netIncome) {
        if (netIncome == null) return "UNKNOWN";
        double val = netIncome.doubleValue();
        if (val < 1500) return "LOW";
        if (val < 3500) return "MEDIUM";
        return "HIGH";
    }
}
