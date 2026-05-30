package com.magenta.banks.infrastructure.adapter.out.persistence.processed;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventJpaEntity, ProcessedEventId> {
}
