package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.model.ZoneType;

import java.util.List;

public interface SearchZonesPort {

    record Query(String text, List<ZoneType> types, int limit) {}

    List<Zone> execute(Query query);
}
