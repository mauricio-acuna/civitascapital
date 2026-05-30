package com.magenta.areas.domain.port.out;

import com.magenta.areas.domain.port.in.CursorPage;
import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.model.ZoneType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZoneRepositoryPort {

    Zone save(Zone zone);

    Optional<Zone> findById(UUID id);

    List<Zone> findChildren(UUID parentId);

    /** Devuelve la cadena completa de ancestors ordenada de root a padre inmediato. */
    List<Zone> findAncestors(UUID id);

    /** Autocomplete: devuelve top-N por relevancia (sin cursor). */
    List<Zone> searchByText(String text, List<ZoneType> types, int limit);

    /**
     * Paginación keyset: ordena por (name ASC, id ASC).
     * {@code afterName} y {@code afterId} son el last-seen del cursor; ambos null en la primera página.
     */
    CursorPage<Zone> listCursor(String textFilter, List<ZoneType> types,
                                int limit, String afterName, UUID afterId);

    List<Zone> findByPostalCode(String postalCode);

    /** Encuentra la zona más específica que contiene el punto (lat, lng). */
    Optional<Zone> resolvePoint(double lat, double lng);

    Optional<Zone> findByCode(String code);
}
