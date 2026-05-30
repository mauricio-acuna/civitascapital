package com.magenta.products.infrastructure.adapter.in.kafka;

import com.magenta.products.application.UpdateFinancingUseCase;
import com.magenta.products.infrastructure.adapter.out.persistence.OutboxEventJpaRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Consumes bank product events to re-evaluate financing feasibility.
 */
@Component
public class BankProductConsumer {

    private static final Logger log = LoggerFactory.getLogger(BankProductConsumer.class);
    private static final String CONSUMER_NAME = "BankProductConsumer";

    private final UpdateFinancingUseCase updateFinancingUseCase;
    private final IdempotencyGuard idempotencyGuard;

    public BankProductConsumer(UpdateFinancingUseCase updateFinancingUseCase,
                                IdempotencyGuard idempotencyGuard) {
        this.updateFinancingUseCase = updateFinancingUseCase;
        this.idempotencyGuard = idempotencyGuard;
    }

    @KafkaListener(topics = "magenta.banks.product.v1",
            groupId = "magenta-products-banks",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consume(ConsumerRecord<String, Map<String, Object>> record, Acknowledgment ack) {
        String eventId = extractEventId(record);
        if (idempotencyGuard.alreadyProcessed(eventId, CONSUMER_NAME)) {
            ack.acknowledge();
            return;
        }
        try {
            Map<String, Object> payload = record.value();
            // Re-evaluate financing for affected properties after a bank product change
            // In practice, this would trigger a batch re-evaluation job
            log.info("Bank product event received, triggering financing re-evaluation");
            idempotencyGuard.markProcessed(eventId, CONSUMER_NAME);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing bank product event: {}", e.getMessage(), e);
            // Do not ack — message will be retried
        }
    }

    private String extractEventId(ConsumerRecord<?, ?> record) {
        return record.topic() + "-" + record.partition() + "-" + record.offset();
    }
}
