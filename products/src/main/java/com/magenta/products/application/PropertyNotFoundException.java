package com.magenta.products.application;

import java.util.UUID;

public class PropertyNotFoundException extends RuntimeException {
    private final UUID propertyId;

    public PropertyNotFoundException(UUID propertyId) {
        super("Property not found: " + propertyId);
        this.propertyId = propertyId;
    }

    public UUID propertyId() { return propertyId; }
}
