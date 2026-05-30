package com.magenta.banks.infrastructure.adapter.out.persistence.euribor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface EuriborRateJpaRepository extends JpaRepository<EuriborRateJpaEntity, LocalDate> {
    Optional<EuriborRateJpaEntity> findFirstByPeriodLessThanEqualOrderByPeriodDesc(LocalDate upTo);
}
