package com.magenta.servicios.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume `magenta.banks.preapproval.v1` y avanza el workflow de mortgage-broker / first-home-aid.
 */
@Component
public class BanksPreapprovalConsumer {

    private static final Logger log = LoggerFactory.getLogger(BanksPreapprovalConsumer.class);

    private final ObjectMapper mapper;

    public BanksPreapprovalConsumer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @KafkaListener(topics = "magenta.banks.preapproval.v1",
                   groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            JsonNode event = mapper.readTree(record.value());
            String orderId = event.path("data").path("orderId").asText();
            String status  = event.path("data").path("status").asText();
            log.info("Preapproval event received: orderId={}, status={}", orderId, status);
            // TODO: advance Zeebe workflow via message correlation
        } catch (Exception e) {
            log.error("Error procesando preapproval event: {}", e.getMessage(), e);
        }
    }
}
