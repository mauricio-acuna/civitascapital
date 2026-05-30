package com.magenta.banks.domain.model.preapproval;

import java.time.Instant;
import java.util.UUID;

/**
 * Value Object: cambio de estado en el historial de una pre-aprobación.
 */
public record StatusChange(
    UUID id,
    String fromStatus,
    String toStatus,
    String reason,
    String actor,
    Instant at
) {}
