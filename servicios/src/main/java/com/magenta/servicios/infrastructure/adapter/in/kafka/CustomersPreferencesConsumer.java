package com.magenta.servicios.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume {@code magenta.customers.preferences.v1}.
 *
 * <p>Recibe cambios en las preferencias del cliente (zona de búsqueda,
 * tipología de inmueble, precio máximo) y refresca los {@code PropertySearchBrief}
 * asociados a órdenes activas del tipo {@code PROPERTY_SEARCH}.
 *
 * <p>Idempotencia: ver tabla {@code processed_message(consumer_name, event_id)} —
 * migración V202506300003 (pendiente S6).
 */
@Component
public class CustomersPreferencesConsumer {

    private static final Logger log = LoggerFactory.getLogger(CustomersPreferencesConsumer.class);
    private static final String CONSUMER_NAME = "customers-preferences-consumer";

    private final ObjectMapper mapper;

    public CustomersPreferencesConsumer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @KafkaListener(topics = "magenta.customers.preferences.v1",
                   groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            JsonNode event      = mapper.readTree(record.value());
            String eventId      = event.path("eventId").asText();
            String customerId   = event.path("data").path("customerId").asText();
            JsonNode prefs      = event.path("data").path("preferences");

            log.info("[{}] preferences event: eventId={} customerId={}",
                    CONSUMER_NAME, eventId, customerId);

            // TODO (S9): verificar idempotencia en processed_message
            // TODO (S9): buscar órdenes IN_PROGRESS de PROPERTY_SEARCH para customerId
            //            y actualizar PropertySearchBrief con las nuevas preferencias
        } catch (Exception e) {
            log.error("[{}] error procesando evento: {}", CONSUMER_NAME, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
