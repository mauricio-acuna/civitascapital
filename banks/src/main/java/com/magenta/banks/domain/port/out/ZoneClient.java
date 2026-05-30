package com.magenta.banks.domain.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto outbound hacia el módulo areas (tipos impositivos por CCAA).
 */
public interface ZoneClient {

    record ZoneInfo(
        UUID zoneId,
        String ccaa,
        BigDecimal ivaPct,
        BigDecimal ajdPct,
        BigDecimal itpPct
    ) {}

    Optional<ZoneInfo> getZoneInfo(UUID zoneId);
}
