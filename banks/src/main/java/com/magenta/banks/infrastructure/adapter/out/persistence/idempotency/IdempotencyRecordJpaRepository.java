package com.magenta.banks.infrastructure.adapter.out.persistence.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordJpaRepository extends JpaRepository<IdempotencyRecordJpaEntity, IdempotencyRecordId> {
}
