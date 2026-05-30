package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.model.EuriborRate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface EuriborRateRepository {
    /** Persiste o actualiza el tipo del período. */
    void save(LocalDate period, BigDecimal rate12mPct, String source);
    /** Último tipo disponible antes o en la fecha dada. */
    Optional<BigDecimal> findLatestRate(LocalDate upTo);
    /** Último registro disponible antes o en la fecha dada. */
    Optional<EuriborRate> findLatest(LocalDate upTo);
    /** Tipo exacto de un período. */
    Optional<BigDecimal> findByPeriod(LocalDate period);
    /** Registro exacto de un período. */
    Optional<EuriborRate> findByPeriodDetails(LocalDate period);
}
