package com.magenta.banks.infrastructure.adapter.out.persistence.preapproval;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PreapprovalJpaRepository extends JpaRepository<PreapprovalJpaEntity, UUID> {
    Page<PreapprovalJpaEntity> findByCustomerId(UUID customerId, Pageable pageable);

    @Query("""
        SELECT p FROM PreapprovalJpaEntity p
        WHERE p.status = 'APPROVED'
          AND p.expiresAt < :now
        """)
    List<PreapprovalJpaEntity> findExpiredApproved(Instant now);
}
