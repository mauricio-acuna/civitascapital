package com.magenta.areas.infrastructure.adapter.out.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.areas.domain.event.PriceIndexPublished;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Productor Kafka para eventos del agregado {@code PriceIndex}.
 *
 * <p>Publica en el topic {@code magenta.areas.price-index.v1} con sobre CloudEvents 1.0.
 * Al igual que {@link ZoneEventProducer}, es complementario al flujo Outbox transaccional
 * ({@link OutboxEventPublisher}) y está destinado a adaptadores de ingesta (Fase 9)
 * que necesitan emitir eventos de forma directa.</p>
 *
 * <p>Clave Kafka: {@code tenantId:zoneId} para que un consumidor pueda procesar
 * todos los eventos de una zona en orden.</p>
 */
@Component
public class PriceIndexEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PriceIndexEventProducer.class);
    static final String TOPIC = "magenta.areas.price-index.v1";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PriceIndexEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper  = objectMapper;
    }

    public void publish(PriceIndexPublished event) {
        send(event.getPriceIndexId(), event.getZoneId(), event.getTenantId(), event.getType(), event);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void send(UUID aggregateId, UUID zoneId, UUID tenantId, String type, Object payload) {
        try {
            String key     = tenantId + ":" + zoneId;
            String message = buildCloudEvent(aggregateId, zoneId, tenantId, type, payload);
            kafkaTemplate.send(TOPIC, key, message);
            log.debug("Published {} to {} key={}", type, TOPIC, key);
        } catch (Exception ex) {
            log.error("Failed to publish {} to {}: {}", type, TOPIC, ex.getMessage(), ex);
            throw new RuntimeException("Kafka publish failed for event " + type, ex);
        }
    }

    private String buildCloudEvent(UUID aggregateId, UUID zoneId, UUID tenantId, String type, Object data) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("specversion",      "1.0");
            envelope.put("id",               UUID.randomUUID().toString());
            envelope.put("source",           "/magenta/areas/price-index");
            envelope.put("type",             type);
            envelope.put("time",             Instant.now().toString());
            envelope.put("subject",          "PriceIndex:" + aggregateId + ":zone:" + zoneId);
            envelope.put("datacontenttype",  "application/json");
            envelope.put("tenantid",         tenantId != null ? tenantId.toString() : "");
            envelope.put("data",             objectMapper.convertValue(data, Object.class));
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialize CloudEvent for " + type, ex);
        }
    }
}
