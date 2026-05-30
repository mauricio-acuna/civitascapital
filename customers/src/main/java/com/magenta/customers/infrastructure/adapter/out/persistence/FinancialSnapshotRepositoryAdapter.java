package com.magenta.customers.infrastructure.adapter.out.persistence;

import com.magenta.customers.domain.model.FinancialSnapshot;
import com.magenta.customers.domain.port.out.FinancialSnapshotRepository;
import com.magenta.customers.infrastructure.adapter.out.persistence.jpa.JpaFinancialSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FinancialSnapshotRepositoryAdapter implements FinancialSnapshotRepository {

    private final JpaFinancialSnapshotRepository jpa;
    private final CustomerPersistenceMapper mapper;

    @Override
    public FinancialSnapshot save(FinancialSnapshot snapshot) {
        var entity = mapper.toFinancialSnapshotEntity(snapshot);
        return mapper.toFinancialSnapshot(jpa.save(entity));
    }

    @Override
    public Optional<FinancialSnapshot> findLatestByCustomerId(UUID customerId) {
        return jpa.findLatestByCustomerId(customerId)
                  .map(mapper::toFinancialSnapshot);
    }

    @Override
    public List<FinancialSnapshot> findAllByCustomerId(UUID customerId) {
        return jpa.findByCustomerId(customerId).stream()
                  .map(mapper::toFinancialSnapshot)
                  .collect(Collectors.toList());
    }
}
