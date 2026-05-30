package com.magenta.banks.infrastructure.adapter.out.persistence.appraisal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "appraisals", schema = "banks")
@Getter
@Setter
public class AppraisalJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(nullable = false, length = 20)
    private String regulation;

    @Column(name = "market_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal marketValue;

    @Column(name = "mortgage_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal mortgageValue;

    @Column(name = "surface_sqm", nullable = false, precision = 8, scale = 2)
    private BigDecimal surfaceSqm;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
