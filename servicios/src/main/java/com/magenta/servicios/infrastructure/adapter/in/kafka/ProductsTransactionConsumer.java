package com.magenta.servicios.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Escucha `magenta.products.transaction.v1` y dispara automáticamente
 * NOTARY_GESTORIA + UTILITIES_SETUP según MODULE-SPEC §8.
 */
@Component
public class ProductsTransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductsTransactionConsumer.class);
    private final ObjectMapper mapper;

    public ProductsTransactionConsumer(ObjectMapper mapper) { this.mapper = mapper; }

    @KafkaListener(topics = "magenta.products.transaction.v1",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            JsonNode event = mapper.readTree(record.value());
            String operationType = event.path("data").path("operationType").asText();
            log.info("Transaction event received, type={}", operationType);
            // TODO: auto-create NOTARY_GESTORIA + UTILITIES_SETUP orders
        } catch (Exception e) {
            log.error("Error procesando transaction event: {}", e.getMessage(), e);
        }
    }
}
