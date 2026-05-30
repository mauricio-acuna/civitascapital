package com.magenta.areas.domain.exception;

import java.util.UUID;

public class ZoneNotFoundException extends RuntimeException {

    public ZoneNotFoundException(UUID id) {
        super("Zone not found: " + id);
    }

    public ZoneNotFoundException(String code) {
        super("Zone not found with code: " + code);
    }
}
