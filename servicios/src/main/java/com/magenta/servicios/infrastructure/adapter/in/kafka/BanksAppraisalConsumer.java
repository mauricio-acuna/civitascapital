package com.magenta.servicios.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BanksAppraisalConsumer {

    private static final Logger log = LoggerFactory.getLogger(BanksAppraisalConsumer.class);
    private final ObjectMapper mapper;

    public BanksAppraisalConsumer(ObjectMapper mapper) { this.mapper = mapper; }

    @KafkaListener(topics = "magenta.banks.appraisal.v1",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            JsonNode event = mapper.readTree(record.value());
            log.info("Appraisal event received for orderId={}", event.path("data").path("orderId").asText());
            // TODO: correlate Zeebe message 'appraisalCompleted'
        } catch (Exception e) {
            log.error("Error procesando appraisal event: {}", e.getMessage(), e);
        }
    }
}
