package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.Property;
import com.magenta.products.domain.model.PropertyStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {
    Property save(Property property);
    Optional<Property> findById(UUID id);
    Optional<Property> findByTenantIdAndReference(UUID tenantId, String reference);
    List<Property> findByZoneId(UUID zoneId);
    void delete(UUID id);
    boolean existsByTenantIdAndReference(UUID tenantId, String reference);
}
