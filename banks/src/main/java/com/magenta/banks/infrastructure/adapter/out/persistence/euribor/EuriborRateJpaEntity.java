package com.magenta.banks.infrastructure.adapter.out.persistence.euribor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "euribor_rates", schema = "banks")
@Getter
@Setter
public class EuriborRateJpaEntity {

    @Id
    @Column(name = "period", nullable = false)
    private LocalDate period;

    @Column(name = "rate_12m_pct", nullable = false, precision = 6, scale = 4)
    private BigDecimal rate12mPct;

    @Column(name = "source", nullable = false, length = 20)
    private String source;
}
