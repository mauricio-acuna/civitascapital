package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.domain.model.*;

import java.time.Instant;
import java.util.UUID;

/** DTO de respuesta para Customer (sin PII sensible). */
public record CustomerResponse(
        UUID id,
        UUID tenantId,
        CustomerType type,
        String displayName,
        CustomerStatus status,
        KycStatusDto kyc,
        Instant createdAt
) {
    public record KycStatusDto(KycStatus status, Integer score, Instant expiresAt) {}

    public static CustomerResponse from(Customer c) {
        KycStatusDto kycDto = null;
        if (c.getKyc() != null) {
            kycDto = new KycStatusDto(
                    c.getKyc().getStatus(),
                    c.getKyc().getScore(),
                    c.getKyc().getExpiresAt());
        }
        return new CustomerResponse(
                c.getId(), c.getTenantId(), c.getType(),
                c.getDisplayName(), c.getStatus(), kycDto, c.getCreatedAt());
    }
}
