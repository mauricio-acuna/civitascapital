package com.magenta.banks.infrastructure.adapter.out.persistence.preapproval;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PreapprovalEventJpaRepository extends JpaRepository<PreapprovalEventJpaEntity, UUID> {
    List<PreapprovalEventJpaEntity> findByPreapprovalIdOrderByAtAsc(UUID preapprovalId);
}
