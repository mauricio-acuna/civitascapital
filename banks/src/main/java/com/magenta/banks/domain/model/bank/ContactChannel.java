package com.magenta.banks.domain.model.bank;

import java.util.UUID;

public record ContactChannel(
    UUID id,
    ContactChannelType type,
    String value,
    String label
) {
    public ContactChannel {
        if (type == null) throw new IllegalArgumentException("type is required");
        if (value == null || value.isBlank()) throw new IllegalArgumentException("value is required");
    }
}
