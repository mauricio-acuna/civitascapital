package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root del Bounded Context.
 * Invariantes:
 *  - type=INDIVIDUAL  → individual non-null, legalEntity/household null
 *  - type=LEGAL_ENTITY → legalEntity non-null, los demás null
 *  - type=HOUSEHOLD   → household non-null, los demás null; ≥1 titular
 */
@Getter
@Builder
@With
public class Customer {

    private final UUID id;
    private final UUID tenantId;
    private final CustomerType type;
    private final String displayName;
    private final CustomerStatus status;
    private final String keycloakUserId;
    private final KycState kyc;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String createdBy;
    private final String updatedBy;
    private final long version;

    // Sub-perfiles — sólo uno estará poblado según type
    private final IndividualProfile individual;
    private final LegalEntityProfile legalEntity;
    private final HouseholdProfile household;

    // ── Validación de invariantes del aggregate ──────────────────────

    public void validateInvariants() {
        switch (type) {
            case INDIVIDUAL -> {
                if (individual == null) throw new IllegalStateException("INDIVIDUAL must have IndividualProfile");
                if (legalEntity != null || household != null) throw new IllegalStateException("INDIVIDUAL must not have other profiles");
            }
            case LEGAL_ENTITY -> {
                if (legalEntity == null) throw new IllegalStateException("LEGAL_ENTITY must have LegalEntityProfile");
                if (individual != null || household != null) throw new IllegalStateException("LEGAL_ENTITY must not have other profiles");
            }
            case HOUSEHOLD -> {
                if (household == null) throw new IllegalStateException("HOUSEHOLD must have HouseholdProfile");
                if (household.getMembers() == null || household.getMembers().isEmpty())
                    throw new IllegalStateException("HOUSEHOLD must have at least one member");
                if (individual != null || legalEntity != null) throw new IllegalStateException("HOUSEHOLD must not have other profiles");
            }
        }
    }
}
