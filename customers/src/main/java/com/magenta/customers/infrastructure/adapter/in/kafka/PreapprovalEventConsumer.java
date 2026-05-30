package com.magenta.customers.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.customers.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Consume eventos de pre-aprobación del módulo `banks` y actualiza timeline del cliente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PreapprovalEventConsumer {

    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "magenta.banks.preapproval.v1",
            groupId = "magenta-customers-preapproval"
    )
    @Transactional
    public void onPreapprovalEvent(@Payload String payload,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.path("type").asText();
            UUID customerId = UUID.fromString(event.path("data").path("customerId").asText());

            log.info("Preapproval event type={} for customer={}", eventType, customerId);
            // Notificación al cliente y actualización de timeline
            // (implementación completa en siguiente iteración)
        } catch (Exception e) {
            log.error("Error processing preapproval event key={}: {}", key, e.getMessage());
        }
    }
}
