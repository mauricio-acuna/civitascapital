package com.magenta.areas.application;

import com.magenta.areas.domain.model.OperationType;
import com.magenta.areas.domain.model.PriceIndex;
import com.magenta.areas.domain.model.PropertyType;
import com.magenta.areas.domain.port.in.CursorPage;
import com.magenta.areas.domain.port.out.PriceIndexRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetPriceIndexUseCase {

    private final PriceIndexRepositoryPort repository;

    public GetPriceIndexUseCase(PriceIndexRepositoryPort repository) {
        this.repository = repository;
    }

    public Optional<PriceIndex> latest(UUID zoneId, PropertyType propertyType, OperationType operationType) {
        return repository.findLatest(zoneId, propertyType, operationType);
    }

    public List<PriceIndex> series(UUID zoneId, PropertyType propertyType,
                                   OperationType operationType, LocalDate from, LocalDate to) {
        return repository.findByZoneAndPeriod(zoneId, propertyType, operationType, from, to);
    }

    public CursorPage<PriceIndex> seriesCursor(UUID zoneId, PropertyType propertyType,
                                                OperationType operationType,
                                                LocalDate from, LocalDate afterPeriod, int limit) {
        int cap = Math.min(limit, 120); // safety cap: max 10 years of monthly data
        return repository.findSeriesCursor(zoneId, propertyType, operationType, from, afterPeriod, cap);
    }
}
