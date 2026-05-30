package com.magenta.areas.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.areas.domain.model.OperationType;
import com.magenta.areas.domain.model.PropertyType;
import com.magenta.areas.domain.port.in.RecomputePriceIndexPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consume transacciones cerradas de products y dispara el recálculo del PriceIndex.
 * Idempotente: persiste event_id en processed_event antes de procesar.
 */
@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final RecomputePriceIndexPort recompute;
    private final ProcessedEventRepository processedEventRepo;
    private final ObjectMapper objectMapper;

    public TransactionConsumer(RecomputePriceIndexPort recompute,
                                ProcessedEventRepository processedEventRepo,
                                ObjectMapper objectMapper) {
        this.recompute          = recompute;
        this.processedEventRepo = processedEventRepo;
        this.objectMapper       = objectMapper;
    }

    @KafkaListener(topics = "magenta.products.transaction.v1",
                   groupId = "areas-transaction-consumer",
                   containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consume(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            JsonNode envelope = objectMapper.readTree(message);
            String eventId = envelope.path("id").asText();

            if (processedEventRepo.isAlreadyProcessed("areas-transaction-consumer", eventId)) {
                log.debug("Skipping already processed event {}", eventId);
                return;
            }

            JsonNode data = envelope.path("data");
            UUID tenantId     = UUID.fromString(envelope.path("tenantid").asText());
            UUID zoneId       = UUID.fromString(data.path("zoneId").asText());
            String propType   = data.path("propertyType").asText("FLAT");
            String opType     = data.path("operationType").asText("SALE");
            LocalDate period  = LocalDate.parse(data.path("period").asText(LocalDate.now().toString()))
                    .withDayOfMonth(1);

            recompute.execute(new RecomputePriceIndexPort.Command(
                    tenantId, zoneId, PropertyType.valueOf(propType),
                    OperationType.valueOf(opType), period));

            processedEventRepo.markProcessed("areas-transaction-consumer", eventId, "magenta.products.transaction.v1");

        } catch (Exception ex) {
            log.error("Error processing transaction event key={}: {}", key, ex.getMessage(), ex);
            throw new RuntimeException(ex); // re-lanzamos para que Kafka reintente
        }
    }
}
