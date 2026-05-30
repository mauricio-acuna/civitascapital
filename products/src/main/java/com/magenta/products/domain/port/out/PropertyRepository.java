package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.Property;
import com.magenta.products.domain.model.OperationType;
import com.magenta.products.domain.model.PropertyStatus;
import com.magenta.products.domain.model.PropertyType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {
    Property save(Property property);
    Optional<Property> findById(UUID id);
    Optional<Property> findByTenantIdAndReference(UUID tenantId, String reference);
    List<Property> findByZoneId(UUID zoneId);
    List<Property> findByTenantIdAndZoneId(UUID tenantId, UUID zoneId);
    List<Property> search(UUID tenantId, PropertyStatus status, PropertyType type, UUID zoneId,
                          OperationType operationType, BigDecimal minPrice, BigDecimal maxPrice, int limit);
    void delete(UUID id);
    boolean existsByTenantIdAndReference(UUID tenantId, String reference);
}
