package com.magenta.banks.infrastructure.adapter.out.persistence.loansimulation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LoanSimulationJpaRepository extends JpaRepository<LoanSimulationJpaEntity, UUID> {

    Page<LoanSimulationJpaEntity> findAllByCustomerId(UUID customerId, Pageable pageable);
}
