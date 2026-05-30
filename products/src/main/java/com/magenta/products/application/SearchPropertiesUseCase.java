package com.magenta.products.application;

import com.magenta.products.domain.model.OperationType;
import com.magenta.products.domain.model.Property;
import com.magenta.products.domain.model.PropertyStatus;
import com.magenta.products.domain.model.PropertyType;
import com.magenta.products.domain.port.out.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SearchPropertiesUseCase {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final PropertyRepository propertyRepository;

    public SearchPropertiesUseCase(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public record Query(
            UUID tenantId,
            PropertyStatus status,
            PropertyType type,
            UUID zoneId,
            OperationType operationType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer limit
    ) {}

    @Transactional(readOnly = true)
    public List<Property> execute(Query query) {
        int resolvedLimit = query.limit() == null ? DEFAULT_LIMIT : query.limit();
        resolvedLimit = Math.max(1, Math.min(MAX_LIMIT, resolvedLimit));

        PropertyStatus status = query.status() != null ? query.status() : PropertyStatus.ACTIVE;
        return propertyRepository.search(
                query.tenantId(),
                status,
                query.type(),
                query.zoneId(),
                query.operationType(),
                query.minPrice(),
                query.maxPrice(),
                resolvedLimit);
    }
}
