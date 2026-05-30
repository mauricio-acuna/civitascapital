package com.magenta.banks.domain.model.preapproval;

import com.magenta.banks.domain.model.PreapprovalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root: Pre-aprobación hipotecaria.
 * El estado solo avanza por transiciones permitidas (PreapprovalStateMachine).
 */
public record Preapproval(
    UUID id,
    UUID tenantId,
    UUID customerId,
    UUID productId,
    UUID propertyId,
    BigDecimal amount,
    int termMonths,
    BigDecimal ltv,
    PreapprovalStatus status,
    List<String> conditions,       // textos de condiciones (avalista, vinculaciones…)
    Instant expiresAt,
    Instant createdAt,
    Instant updatedAt,
    List<StatusChange> history,
    long version
) {
    public Preapproval {
        if (customerId == null) throw new IllegalArgumentException("customerId is required");
        if (productId == null)  throw new IllegalArgumentException("productId is required");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
        history    = history    == null ? List.of() : List.copyOf(history);
    }

    /**
     * Aplica una transición de estado; devuelve la nueva instancia con el historial actualizado.
     */
    public Preapproval transition(PreapprovalStatus newStatus, String reason, String actor) {
        StatusChange change = new StatusChange(UUID.randomUUID(),
                status == null ? null : status.name(),
                newStatus.name(), reason, actor, Instant.now());
        List<StatusChange> newHistory = new java.util.ArrayList<>(history);
        newHistory.add(change);
        return new Preapproval(id, tenantId, customerId, productId, propertyId,
                amount, termMonths, ltv, newStatus, conditions,
                expiresAt, createdAt, Instant.now(), List.copyOf(newHistory), version + 1);
    }
}
