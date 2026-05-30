package com.magenta.areas.domain.port.in;

import java.util.List;

/**
 * Resultado de una consulta paginada por cursor (keyset pagination).
 * {@code nextCursor} es null cuando no hay más páginas.
 */
public record CursorPage<T>(List<T> items, String nextCursor) {

    public boolean hasMore() {
        return nextCursor != null;
    }
}
