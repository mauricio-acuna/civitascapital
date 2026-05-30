package com.magenta.banks.domain.model.pagination;

public record PageSpec(
        int page,
        int size
) {
    public static PageSpec of(int page, int size) {
        return new PageSpec(Math.max(0, page), Math.max(1, size));
    }
}
