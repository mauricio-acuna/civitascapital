package com.magenta.products.domain.model;

import java.util.List;
import java.util.UUID;

public class PropertyPublishException extends RuntimeException {

    private final UUID propertyId;
    private final List<String> violations;

    public PropertyPublishException(UUID propertyId, List<String> violations) {
        super("Cannot publish property " + propertyId + ": " + violations);
        this.propertyId = propertyId;
        this.violations = List.copyOf(violations);
    }

    public UUID propertyId() { return propertyId; }
    public List<String> violations() { return violations; }
}
