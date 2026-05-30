package com.magenta.products.domain.event;

import com.magenta.products.domain.model.OperationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OperationPublished(UUID operationId, UUID propertyId, UUID tenantId,
                                  OperationType type, BigDecimal price, Instant occurredAt) {}

