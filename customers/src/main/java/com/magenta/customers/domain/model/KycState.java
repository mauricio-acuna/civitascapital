package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@With
public class KycState {

    private final KycStatus status;
    private final KycProvider provider;
    private final String idDocumentType;
    /** El número de documento se almacena cifrado; en dominio llega en claro. */
    private final String idDocumentNumber;
    private final Map<String, Boolean> checks;   // documentAuthentic, livenessOk, sanctionsClean, pepFlag, addressVerified
    private final Integer score;
    private final Instant verifiedAt;
    private final Instant expiresAt;
    private final String providerRef;
}
