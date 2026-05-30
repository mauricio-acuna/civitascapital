package com.magenta.customers.domain.port.out;

import com.magenta.customers.domain.model.FinancialSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialSnapshotRepository {
    FinancialSnapshot save(FinancialSnapshot snapshot);
    Optional<FinancialSnapshot> findLatestByCustomerId(UUID customerId);
    List<FinancialSnapshot> findAllByCustomerId(UUID customerId);
}
