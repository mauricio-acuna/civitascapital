package com.magenta.customers.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consume eventos de zonas del módulo `areas` para reescribir zone_ids deprecados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ZoneEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "magenta.areas.zone.v1",
            groupId = "magenta-customers-zones"
    )
    @Transactional
    public void onZoneEvent(@Payload String payload,
                            @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.path("type").asText();

            if (eventType.contains("ZoneDeprecated") || eventType.contains("ZoneMerged")) {
                // Actualizar zone_id en individual_profiles y search_preferences
                log.info("Zone event received type={} — zone_id rewrite pending", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing zone event key={}: {}", key, e.getMessage());
        }
    }
}
