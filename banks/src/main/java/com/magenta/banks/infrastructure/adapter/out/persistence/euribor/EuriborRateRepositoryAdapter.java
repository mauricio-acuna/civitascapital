package com.magenta.banks.infrastructure.adapter.out.persistence.euribor;

import com.magenta.banks.domain.model.EuriborRate;
import com.magenta.banks.domain.port.out.EuriborRateRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class EuriborRateRepositoryAdapter implements EuriborRateRepository {

    private final EuriborRateJpaRepository jpaRepository;

    public EuriborRateRepositoryAdapter(EuriborRateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(LocalDate period, BigDecimal rate12mPct, String source) {
        EuriborRateJpaEntity entity = new EuriborRateJpaEntity();
        entity.setPeriod(period);
        entity.setRate12mPct(rate12mPct);
        entity.setSource(source);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<BigDecimal> findLatestRate(LocalDate upTo) {
        return findLatest(upTo).map(EuriborRate::rate12mPct);
    }

    @Override
    public Optional<EuriborRate> findLatest(LocalDate upTo) {
        return jpaRepository.findFirstByPeriodLessThanEqualOrderByPeriodDesc(upTo)
                .map(this::toDomain);
    }

    @Override
    public Optional<BigDecimal> findByPeriod(LocalDate period) {
        return findByPeriodDetails(period).map(EuriborRate::rate12mPct);
    }

    @Override
    public Optional<EuriborRate> findByPeriodDetails(LocalDate period) {
        return jpaRepository.findById(period).map(this::toDomain);
    }

    private EuriborRate toDomain(EuriborRateJpaEntity entity) {
        return new EuriborRate(entity.getPeriod(), entity.getRate12mPct(), entity.getSource());
    }
}
