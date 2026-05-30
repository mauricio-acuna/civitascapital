package com.magenta.servicios.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query("SELECT e FROM OutboxEventJpaEntity e WHERE e.publishedAt IS NULL ORDER BY e.createdAt ASC")
    List<OutboxEventJpaEntity> findUnpublished();

    @Modifying
    @Query("UPDATE OutboxEventJpaEntity e SET e.publishedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void markPublished(@Param("id") UUID id);
}
