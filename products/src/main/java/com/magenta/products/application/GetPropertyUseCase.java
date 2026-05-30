package com.magenta.products.application;

import com.magenta.products.domain.model.Property;
import com.magenta.products.domain.port.out.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetPropertyUseCase {

    private final PropertyRepository propertyRepository;

    public GetPropertyUseCase(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Transactional(readOnly = true)
    public Property execute(UUID tenantId, UUID propertyId) {
        return propertyRepository.findByTenantIdAndId(tenantId, propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));
    }
}
