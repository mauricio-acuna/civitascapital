package com.magenta.servicios.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {

    public enum Direction { INBOUND, OUTBOUND }

    public enum Status {
        PENDING, AUTHORIZED, CAPTURED, REFUNDED, FAILED
    }

    public enum Method { CARD, SEPA, WALLET }

    private final UUID id;
    private final UUID orderId;
    private final Direction direction;
    private final BigDecimal amount;
    private final String currency;
    private final Method method;
    private String providerRef;
    private Status status;
    private final BigDecimal vatPct;
    private String invoiceNumber;
    private final Instant at;

    public Payment(UUID id, UUID orderId, Direction direction, BigDecimal amount,
                   String currency, Method method, String providerRef, Status status,
                   BigDecimal vatPct, String invoiceNumber, Instant at) {
        this.id = id;
        this.orderId = orderId;
        this.direction = direction;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.providerRef = providerRef;
        this.status = status;
        this.vatPct = vatPct;
        this.invoiceNumber = invoiceNumber;
        this.at = at;
    }

    public void capture(String providerRef) {
        this.providerRef = providerRef;
        this.status = Status.CAPTURED;
    }

    public void refund() {
        this.status = Status.REFUNDED;
    }

    public void fail() {
        this.status = Status.FAILED;
    }

    public void assignInvoiceNumber(String number) {
        this.invoiceNumber = number;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public Direction getDirection() { return direction; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Method getMethod() { return method; }
    public String getProviderRef() { return providerRef; }
    public Status getStatus() { return status; }
    public BigDecimal getVatPct() { return vatPct; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Instant getAt() { return at; }
}
