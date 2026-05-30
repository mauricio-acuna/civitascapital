package com.magenta.areas.domain.port.in;

import java.util.UUID;

public interface DeleteZonePort {

    void execute(UUID id, String actorId);
}
