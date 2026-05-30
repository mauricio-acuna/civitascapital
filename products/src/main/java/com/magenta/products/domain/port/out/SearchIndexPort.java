package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.Property;

import java.util.List;
import java.util.UUID;

public interface SearchIndexPort {
    void index(Property property);
    void delete(UUID propertyId);
    void reindexByZone(UUID zoneId, List<Property> properties);
}
