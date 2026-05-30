package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.model.loansimulation.LoanSimulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface LoanSimulationRepository {
    LoanSimulation save(LoanSimulation simulation);
    Optional<LoanSimulation> findById(UUID id);
    Page<LoanSimulation> findByCustomerId(UUID customerId, Pageable pageable);
}
