package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.PriceIndex;
import com.magenta.areas.domain.model.ZoneEnrichment;
import com.magenta.areas.domain.model.Zone;

import java.util.List;
import java.util.UUID;

public interface CompareZonesPort {

    record ZoneComparison(Zone zone, PriceIndex latestSale, PriceIndex latestRent,
                          ZoneEnrichment enrichment) {}

    List<ZoneComparison> execute(List<UUID> zoneIds);
}
