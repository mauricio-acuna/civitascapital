package com.magenta.products.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "operations", schema = "products")
@Getter @Setter
public class OperationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyJpaEntity property;

    @Column(name = "type", nullable = false, length = 16)
    private String type;

    @Column(name = "price", nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "deposit_months")
    private Integer depositMonths;

    @Column(name = "min_contract_mo")
    private Integer minContractMonths;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "rent_to_own", columnDefinition = "jsonb")
    private String rentToOwn;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "exchange_wishes", columnDefinition = "jsonb")
    private String exchangeWishes;

    @Column(name = "negotiable", nullable = false)
    private boolean negotiable;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "commission_pct", precision = 5, scale = 2)
    private BigDecimal commissionPct;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "exclusivity", nullable = false)
    private boolean exclusivity;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
