package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.Zone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetZonePort {

    Optional<Zone> byId(UUID id);

    List<Zone> children(UUID parentId);

    List<Zone> ancestors(UUID id);

    List<Zone> byPostalCode(String postalCode);
}
