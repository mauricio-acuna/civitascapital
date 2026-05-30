package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.model.loansimulation.LoanSimulation;
import com.magenta.banks.domain.model.pagination.PageResult;
import com.magenta.banks.domain.model.pagination.PageSpec;

import java.util.Optional;
import java.util.UUID;

public interface LoanSimulationRepository {
    LoanSimulation save(LoanSimulation simulation);
    Optional<LoanSimulation> findById(UUID id);
    PageResult<LoanSimulation> findByCustomerId(UUID customerId, PageSpec page);
}
