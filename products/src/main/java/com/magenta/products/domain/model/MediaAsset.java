package com.magenta.products.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MediaAsset(
        UUID id,
        UUID propertyId,
        MediaKind kind,
        String storageUri,
        String mimeType,
        long sizeBytes,
        Integer width,
        Integer height,
        List<String> aiTags,
        int order,
        boolean isCover,
        Instant uploadedAt) {
}
