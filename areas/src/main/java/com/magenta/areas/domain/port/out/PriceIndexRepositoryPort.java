package com.magenta.areas.domain.port.out;

import com.magenta.areas.domain.port.in.CursorPage;
import com.magenta.areas.domain.model.OperationType;
import com.magenta.areas.domain.model.PriceIndex;
import com.magenta.areas.domain.model.PropertyType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceIndexRepositoryPort {

    void save(PriceIndex priceIndex);

    Optional<PriceIndex> findLatest(UUID zoneId, PropertyType propertyType, OperationType operationType);

    List<PriceIndex> findByZoneAndPeriod(UUID zoneId, PropertyType propertyType,
                                          OperationType operationType,
                                          LocalDate from, LocalDate to);

    /**
     * Paginación keyset sobre el feed de price-indices, ordenado por period ASC.
     * {@code afterPeriod} es el último período visto; null en la primera página.
     */
    CursorPage<PriceIndex> findSeriesCursor(UUID zoneId, PropertyType propertyType,
                                             OperationType operationType,
                                             LocalDate from, LocalDate afterPeriod, int limit);

    /** Recupera las muestras crudas de precio/m² desde las transacciones cerradas. */
    List<java.math.BigDecimal> fetchTransactionSamples(UUID zoneId, PropertyType propertyType,
                                                        OperationType operationType, LocalDate period);
}
