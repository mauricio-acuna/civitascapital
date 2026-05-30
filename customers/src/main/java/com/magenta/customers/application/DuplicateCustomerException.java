package com.magenta.customers.application;

/**
 * Se lanza cuando se intenta registrar un cliente con NIF o email ya existente.
 */
public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String message) {
        super(message);
    }
}
