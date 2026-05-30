package com.magenta.products.infrastructure.adapter.in.kafka;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Idempotency guard for Kafka consumers using the processed_event table.
 */
@Component
public class IdempotencyGuard {

    private final EntityManager em;

    public IdempotencyGuard(EntityManager em) {
        this.em = em;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean alreadyProcessed(String eventId, String consumer) {
        Long count = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products.processed_event WHERE event_id = :id AND consumer = :consumer",
                Long.class)
                .setParameter("id", java.util.UUID.nameUUIDFromBytes((eventId + consumer).getBytes()))
                .setParameter("consumer", consumer)
                .getSingleResult();
        return count > 0;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void markProcessed(String eventId, String consumer) {
        em.createNativeQuery(
                "INSERT INTO products.processed_event (event_id, consumer, processed_at) VALUES (:id, :consumer, :ts) ON CONFLICT DO NOTHING")
                .setParameter("id", java.util.UUID.nameUUIDFromBytes((eventId + consumer).getBytes()))
                .setParameter("consumer", consumer)
                .setParameter("ts", Instant.now())
                .executeUpdate();
    }
}
