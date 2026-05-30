package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@With
public class Consent {

    private final UUID id;
    private final UUID customerId;
    private final String purpose;        // marketing, profiling, share_with_bank, …
    private final boolean granted;
    private final Instant grantedAt;
    private final Instant revokedAt;
    private final String legalBasis;     // art.6.1.a..f RGPD
    private final Map<String, Object> evidence;  // IP, user-agent, form version
}
