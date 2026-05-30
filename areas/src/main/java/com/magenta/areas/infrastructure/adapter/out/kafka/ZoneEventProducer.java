package com.magenta.areas.infrastructure.adapter.out.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.areas.domain.event.ZoneCreated;
import com.magenta.areas.domain.event.ZoneDeprecated;
import com.magenta.areas.domain.event.ZoneUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Productor Kafka para eventos del agregado {@code Zone}.
 *
 * <p>Publica en el topic {@code magenta.areas.zone.v1} con sobre CloudEvents 1.0.
 * El flujo normal pasa por el Outbox transaccional ({@link OutboxEventPublisher});
 * este bean es usado por los adaptadores de ingesta (Fase 9) cuando necesitan emitir
 * eventos fuera de una transacción de escritura, por ejemplo en proyecciones o
 * re-publicaciones programadas.</p>
 *
 * <p>Clave Kafka: {@code tenantId:zoneId} para particionado por tenant.</p>
 */
@Component
public class ZoneEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ZoneEventProducer.class);
    static final String TOPIC = "magenta.areas.zone.v1";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ZoneEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper  = objectMapper;
    }

    public void publish(ZoneCreated event) {
        send(event.getZoneId(), event.getTenantId(), event.getType(), event);
    }

    public void publish(ZoneUpdated event) {
        send(event.getZoneId(), event.getTenantId(), event.getType(), event);
    }

    public void publish(ZoneDeprecated event) {
        send(event.getZoneId(), event.getTenantId(), event.getType(), event);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void send(UUID aggregateId, UUID tenantId, String type, Object payload) {
        try {
            String key     = tenantId + ":" + aggregateId;
            String message = buildCloudEvent(aggregateId, tenantId, type, payload);
            kafkaTemplate.send(TOPIC, key, message);
            log.debug("Published {} to {} key={}", type, TOPIC, key);
        } catch (Exception ex) {
            log.error("Failed to publish {} to {}: {}", type, TOPIC, ex.getMessage(), ex);
            throw new RuntimeException("Kafka publish failed for event " + type, ex);
        }
    }

    private String buildCloudEvent(UUID aggregateId, UUID tenantId, String type, Object data) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("specversion",      "1.0");
            envelope.put("id",               UUID.randomUUID().toString());
            envelope.put("source",           "/magenta/areas/zones");
            envelope.put("type",             type);
            envelope.put("time",             Instant.now().toString());
            envelope.put("subject",          "Zone:" + aggregateId);
            envelope.put("datacontenttype",  "application/json");
            envelope.put("tenantid",         tenantId != null ? tenantId.toString() : "");
            envelope.put("data",             objectMapper.convertValue(data, Object.class));
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialize CloudEvent for " + type, ex);
        }
    }
}
