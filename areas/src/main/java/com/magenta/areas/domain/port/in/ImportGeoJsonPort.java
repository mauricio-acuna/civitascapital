package com.magenta.areas.domain.port.in;

import java.io.InputStream;
import java.util.UUID;

public interface ImportGeoJsonPort {

    record Command(UUID tenantId, InputStream geojson, String originalFilename,
                   long fileSizeBytes, String sha256, String actorId) {}

    /** Devuelve el número de zonas importadas/actualizadas. */
    int execute(Command command);
}
