package com.magenta.servicios.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AreasZoneConsumer {

    private static final Logger log = LoggerFactory.getLogger(AreasZoneConsumer.class);
    private final ObjectMapper mapper;

    public AreasZoneConsumer(ObjectMapper mapper) { this.mapper = mapper; }

    @KafkaListener(topics = "magenta.areas.zone.v1",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            JsonNode event = mapper.readTree(record.value());
            String zoneId = event.path("data").path("zoneId").asText();
            log.info("Zone change event: zoneId={}", zoneId);
            // TODO: reassign partners whose coverageZoneIds changed
        } catch (Exception e) {
            log.error("Error procesando zone event: {}", e.getMessage(), e);
        }
    }
}
