package com.magenta.banks.domain.model.bank;

import com.magenta.banks.domain.model.Rating;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root: Entidad financiera.
 * El dominio es inmutable; las modificaciones producen una nueva instancia.
 */
public record Bank(
    UUID id,
    UUID tenantId,
    String code,          // BIC o código interno
    String name,
    String brand,
    String country,
    String bdeRegistryNumber,
    Rating rating,
    String logoUrl,
    String websiteUrl,
    boolean active,
    List<ContactChannel> contactChannels,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy,
    long version
) {
    public Bank {
        if (code == null || code.isBlank())   throw new IllegalArgumentException("code is required");
        if (name == null || name.isBlank())   throw new IllegalArgumentException("name is required");
        if (tenantId == null)                 throw new IllegalArgumentException("tenantId is required");
        contactChannels = contactChannels == null ? List.of() : List.copyOf(contactChannels);
    }

    public Bank activate() {
        return new Bank(id, tenantId, code, name, brand, country, bdeRegistryNumber,
                rating, logoUrl, websiteUrl, true, contactChannels,
                createdAt, Instant.now(), createdBy, updatedBy, version + 1);
    }

    public Bank deactivate() {
        return new Bank(id, tenantId, code, name, brand, country, bdeRegistryNumber,
                rating, logoUrl, websiteUrl, false, contactChannels,
                createdAt, Instant.now(), createdBy, updatedBy, version + 1);
    }
}
