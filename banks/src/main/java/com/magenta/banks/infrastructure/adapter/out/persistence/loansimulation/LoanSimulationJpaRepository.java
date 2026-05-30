package com.magenta.banks.infrastructure.adapter.out.persistence.loansimulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanSimulationJpaRepository extends JpaRepository<LoanSimulationJpaEntity, UUID> {

    List<LoanSimulationJpaEntity> findAllByCustomerId(UUID customerId);
}
