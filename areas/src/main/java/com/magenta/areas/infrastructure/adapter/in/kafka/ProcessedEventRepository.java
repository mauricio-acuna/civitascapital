package com.magenta.areas.infrastructure.adapter.in.kafka;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Gestiona la tabla processed_event para garantizar idempotencia de consumidores Kafka.
 *
 * <p>La PK es compuesta {@code (consumer_name, event_id)} para que cada consumidor
 * mantenga su propio registro de eventos procesados de forma independiente. Así,
 * si TransactionConsumer procesa "evt-1", PropertyConsumer puede procesarlo también
 * sin que sea bloqueado por la comprobación de idempotencia del otro consumidor.
 */
@Repository
public class ProcessedEventRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * @param consumerName nombre lógico del consumidor (p.ej. "areas-transaction-consumer")
     * @param eventId      identificador del evento CloudEvents (campo {@code id})
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean isAlreadyProcessed(String consumerName, String eventId) {
        Long count = em.createNativeQuery(
                        "SELECT COUNT(*) FROM areas.processed_event " +
                        "WHERE consumer_name = :consumer AND event_id = :id",
                        Long.class)
                .setParameter("consumer", consumerName)
                .setParameter("id", java.util.UUID.fromString(eventId))
                .getSingleResult();
        return count > 0;
    }

    /**
     * @param consumerName nombre lógico del consumidor
     * @param eventId      identificador del evento
     * @param topic        topic Kafka del que procede el evento
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void markProcessed(String consumerName, String eventId, String topic) {
        em.createNativeQuery("""
            INSERT INTO areas.processed_event(consumer_name, event_id, topic, processed_at)
            VALUES (:consumer, :id, :topic, :now)
            ON CONFLICT (consumer_name, event_id) DO NOTHING
            """)
                .setParameter("consumer", consumerName)
                .setParameter("id", java.util.UUID.fromString(eventId))
                .setParameter("topic", topic)
                .setParameter("now", Instant.now())
                .executeUpdate();
    }
}
