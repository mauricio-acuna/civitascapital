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
public class DocumentRef {

    private final UUID id;
    private final UUID customerId;
    private final DocumentKind kind;
    private final String filename;
    private final String mimeType;
    private final long sizeBytes;
    private final String storageUri;
    private final String sha256;
    private final Map<String, Object> ocrParsed;
    private final ValidationStatus validationStatus;
    private final Instant uploadedAt;
    private final Instant expiresAt;
}
