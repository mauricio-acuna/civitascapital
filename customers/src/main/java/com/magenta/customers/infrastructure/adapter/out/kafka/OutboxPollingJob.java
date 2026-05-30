package com.magenta.customers.infrastructure.adapter.out.kafka;

import com.magenta.customers.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.magenta.customers.infrastructure.adapter.out.persistence.jpa.JpaOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Lee periódicamente la tabla outbox_event y publica en Kafka.
 * Transaccional para garantizar exactamente-una-vez (at-least-once + idempotencia del consumidor).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPollingJob {

    private static final String TOPIC_PROFILE = "magenta.customers.profile.v1";
    private static final String TOPIC_KYC = "magenta.customers.kyc.v1";
    private static final String TOPIC_PREFS = "magenta.customers.preferences.v1";
    private static final String TOPIC_RGPD = "magenta.customers.rgpd.v1";

    private final JpaOutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${magenta.outbox.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${magenta.outbox.polling-delay-ms:5000}")
    @Transactional
    public void poll() {
        List<OutboxEventJpaEntity> pending = outboxRepo.findUnpublished(batchSize);
        if (pending.isEmpty()) return;

        for (OutboxEventJpaEntity event : pending) {
            try {
                String topic = topicFor(event.getType());
                kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to publish event {} to {}: {}", event.getId(), topic, ex.getMessage());
                            }
                        });
                outboxRepo.markPublished(event.getId(), Instant.now());
            } catch (Exception e) {
                log.error("Error processing outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
        log.debug("Outbox poll: published {} events", pending.size());
    }

    private String topicFor(String eventType) {
        if (eventType.contains("Kyc")) return TOPIC_KYC;
        if (eventType.contains("SearchPreference")) return TOPIC_PREFS;
        if (eventType.contains("Consent") || eventType.contains("Erasure")) return TOPIC_RGPD;
        return TOPIC_PROFILE;
    }
}
