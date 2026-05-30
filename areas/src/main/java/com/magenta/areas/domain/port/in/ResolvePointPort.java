package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.Zone;

import java.util.Optional;

public interface ResolvePointPort {

    Optional<Zone> execute(double lat, double lng);
}
