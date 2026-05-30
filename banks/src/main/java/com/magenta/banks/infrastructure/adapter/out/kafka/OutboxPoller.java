package com.magenta.banks.infrastructure.adapter.out.kafka;

import com.magenta.banks.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import com.magenta.banks.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Publica los eventos pendientes en Kafka cada 5 segundos.
 * Marca cada evento como publicado al completarse el envío.
 */
@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private static final String TOPIC_PREFIX = "magenta.banks.";

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPoller(OutboxEventJpaRepository outboxRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate    = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "5000")
    @Transactional
    public void pollAndPublish() {
        List<OutboxEventJpaEntity> pending = outboxRepository.findUnpublished();
        if (pending.isEmpty()) return;

        for (OutboxEventJpaEntity event : pending) {
            try {
                String topic = resolveTopic(event.getType());
                kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to publish event {} to topic {}", event.getId(), topic, ex);
                            }
                        });
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Error processing outbox event {}", event.getId(), e);
            }
        }
    }

    private String resolveTopic(String eventType) {
        if (eventType == null) return TOPIC_PREFIX + "unknown.v1";
        if (eventType.contains("Product"))      return "magenta.banks.product.v1";
        if (eventType.contains("Preapproval"))  return "magenta.banks.preapproval.v1";
        if (eventType.contains("Appraisal"))    return "magenta.banks.appraisal.v1";
        if (eventType.contains("Simulation"))   return "magenta.banks.simulation.v1";
        return TOPIC_PREFIX + "unknown.v1";
    }
}
