package com.magenta.servicios.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume {@code magenta.products.property.v1}.
 *
 * <p>Recibe actualizaciones de propiedades (valoración, estado, contrato)
 * y activa ofertas de cross-sell si la propiedad entra en fase de venta/alquiler
 * y el cliente no tiene aún órdenes de servicios asociadas.
 *
 * <p>Idempotencia: ver tabla {@code processed_message(consumer_name, event_id)} —
 * migración V202506300003 (pendiente S6).
 */
@Component
public class ProductsPropertyConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductsPropertyConsumer.class);
    private static final String CONSUMER_NAME = "products-property-consumer";

    private final ObjectMapper mapper;

    public ProductsPropertyConsumer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @KafkaListener(topics = "magenta.products.property.v1",
                   groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            JsonNode event  = mapper.readTree(record.value());
            String eventId  = event.path("eventId").asText();
            String propId   = event.path("data").path("propertyId").asText();
            String propStatus = event.path("data").path("status").asText();

            log.info("[{}] property event: eventId={} propertyId={} status={}",
                    CONSUMER_NAME, eventId, propId, propStatus);

            // TODO (S9): verificar idempotencia en processed_message
            // TODO (S9): si status=ON_SALE o status=FOR_RENT, activar cross-sell
            //            creando ServiceOrders de APPRAISAL / LEGAL_REVIEW si no existen
        } catch (Exception e) {
            log.error("[{}] error procesando evento: {}", CONSUMER_NAME, e.getMessage(), e);
            throw new RuntimeException(e); // re-throw para que el DLT capture el mensaje
        }
    }
}
