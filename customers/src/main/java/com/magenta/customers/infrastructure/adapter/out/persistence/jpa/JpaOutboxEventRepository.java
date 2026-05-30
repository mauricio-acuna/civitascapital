package com.magenta.customers.infrastructure.adapter.out.persistence.jpa;

import com.magenta.customers.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query("SELECT o FROM OutboxEventJpaEntity o WHERE o.publishedAt IS NULL ORDER BY o.createdAt LIMIT :size")
    List<OutboxEventJpaEntity> findUnpublished(@Param("size") int size);

    @Modifying
    @Query("UPDATE OutboxEventJpaEntity o SET o.publishedAt = :now WHERE o.id = :id")
    void markPublished(@Param("id") UUID id, @Param("now") Instant now);
}
