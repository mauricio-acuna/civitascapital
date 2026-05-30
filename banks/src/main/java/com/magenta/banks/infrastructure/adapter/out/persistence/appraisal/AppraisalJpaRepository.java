package com.magenta.banks.infrastructure.adapter.out.persistence.appraisal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppraisalJpaRepository extends JpaRepository<AppraisalJpaEntity, UUID> {
    List<AppraisalJpaEntity> findByPropertyId(UUID propertyId);
    List<AppraisalJpaEntity> findByCustomerId(UUID customerId);
    Optional<AppraisalJpaEntity> findFirstByPropertyIdAndValidUntilAfterOrderByIssuedAtDesc(UUID propertyId, LocalDate today);
}
