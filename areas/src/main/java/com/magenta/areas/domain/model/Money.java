package com.magenta.areas.domain.model;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Value Object inmutable que representa una cantidad monetaria con divisa.
 * Sin dependencias de Spring ni JPA — dominio puro.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency must not be blank");
        // Validamos que la divisa sea ISO 4217 válida
        Currency.getInstance(currency); // lanza IllegalArgumentException si no es válida
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money euros(BigDecimal amount) {
        return new Money(amount, "EUR");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add amounts with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
