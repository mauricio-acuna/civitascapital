package com.magenta.customers.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.customers.domain.port.out.SearchPreferenceRepository;
import com.magenta.customers.infrastructure.adapter.out.persistence.jpa.JpaOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Consume eventos del módulo `products` para matching de búsquedas guardadas (UC-C8).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PropertyEventConsumer {

    private final SearchPreferenceRepository preferenceRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "magenta.products.property.v1",
            groupId = "magenta-customers-property",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onPropertyEvent(@Payload String payload,
                                @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.path("type").asText();

            if ("com.magenta.products.PropertyPublished".equals(eventType)) {
                processPropertyPublished(event);
            }
        } catch (Exception e) {
            log.error("Error processing property event key={}: {}", key, e.getMessage());
        }
    }

    private void processPropertyPublished(JsonNode event) {
        // Matching asíncrono: en producción usaría el MatchEngine nightly
        // Aquí registramos para trazabilidad
        log.debug("Property published event received, will be processed by nightly MatchEngine");
    }
}
