package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.EuriborRate;
import com.magenta.banks.domain.port.out.EuriborRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
public class GetEuriborRateUseCase {

    private final EuriborRateRepository euriborRateRepository;

    public GetEuriborRateUseCase(EuriborRateRepository euriborRateRepository) {
        this.euriborRateRepository = euriborRateRepository;
    }

    public record Result(LocalDate period, BigDecimal rate12mPct, String source) {}

    @Transactional(readOnly = true)
    public Result latest(LocalDate upTo) {
        LocalDate effectiveDate = upTo != null ? upTo : LocalDate.now();
        return euriborRateRepository.findLatest(effectiveDate)
                .map(this::toResult)
                .orElseThrow(() -> new NoSuchElementException("No Euribor rate available up to " + effectiveDate));
    }

    @Transactional(readOnly = true)
    public Result byPeriod(LocalDate period) {
        return euriborRateRepository.findByPeriodDetails(period)
                .map(this::toResult)
                .orElseThrow(() -> new NoSuchElementException("No Euribor rate available for " + period));
    }

    private Result toResult(EuriborRate rate) {
        return new Result(rate.period(), rate.rate12mPct(), rate.source());
    }
}
