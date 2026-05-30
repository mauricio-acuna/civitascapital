package com.magenta.servicios.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code services.payments}.
 * Sin campo {@code version}: los pagos son eventos contables inmutables;
 * sólo se permite actualizar {@code status}, {@code provider_ref} e {@code invoice_number}.
 */
@Entity
@Table(name = "payments", schema = "services")
public class PaymentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(nullable = false, length = 10)
    private String direction;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 16)
    private String method;

    @Column(name = "provider_ref", length = 120)
    private String providerRef;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "vat_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatPct;

    @Column(name = "invoice_number", length = 40)
    private String invoiceNumber;

    @Column(nullable = false, updatable = false)
    private Instant at;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getProviderRef() { return providerRef; }
    public void setProviderRef(String providerRef) { this.providerRef = providerRef; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getVatPct() { return vatPct; }
    public void setVatPct(BigDecimal vatPct) { this.vatPct = vatPct; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Instant getAt() { return at; }
    public void setAt(Instant at) { this.at = at; }
}
