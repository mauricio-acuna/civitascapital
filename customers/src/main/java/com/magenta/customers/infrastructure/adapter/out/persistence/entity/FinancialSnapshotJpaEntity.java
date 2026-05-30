package com.magenta.customers.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "financial_snapshots", schema = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialSnapshotJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @Column(name = "as_of", nullable = false)
    private LocalDate asOf;

    @Column(name = "net_income", nullable = false, precision = 12, scale = 2)
    private BigDecimal netIncome;

    @Column(nullable = false)
    private short payments;

    @Column(name = "gross_income_yr", precision = 12, scale = 2)
    private BigDecimal grossIncomeYr;

    @Column(name = "other_debt", nullable = false, precision = 12, scale = 2)
    private BigDecimal otherDebt;

    @Column(name = "cirbe_flag", nullable = false)
    private boolean cirbeFlag;

    @Column(name = "own_funds", nullable = false, precision = 14, scale = 2)
    private BigDecimal ownFunds;

    @Column(name = "existing_props", nullable = false)
    private int existingProps;

    @Column(name = "rental_income", nullable = false, precision = 12, scale = 2)
    private BigDecimal rentalIncome;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal confidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String computed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }
}
