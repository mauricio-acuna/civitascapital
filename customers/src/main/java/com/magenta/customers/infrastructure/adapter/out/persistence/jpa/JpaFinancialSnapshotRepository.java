package com.magenta.customers.infrastructure.adapter.out.persistence.jpa;

import com.magenta.customers.infrastructure.adapter.out.persistence.entity.FinancialSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaFinancialSnapshotRepository extends JpaRepository<FinancialSnapshotJpaEntity, UUID> {

    @Query("SELECT fs FROM FinancialSnapshotJpaEntity fs WHERE fs.customer.id = :customerId ORDER BY fs.asOf DESC LIMIT 1")
    Optional<FinancialSnapshotJpaEntity> findLatestByCustomerId(@Param("customerId") UUID customerId);

    List<FinancialSnapshotJpaEntity> findByCustomerIdOrderByAsOfDesc(UUID customerId);
}
