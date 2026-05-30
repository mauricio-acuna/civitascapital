package com.magenta.servicios.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CustomersKycConsumer {

    private static final Logger log = LoggerFactory.getLogger(CustomersKycConsumer.class);
    private final ObjectMapper mapper;

    public CustomersKycConsumer(ObjectMapper mapper) { this.mapper = mapper; }

    @KafkaListener(topics = "magenta.customers.kyc.v1",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            JsonNode event = mapper.readTree(record.value());
            String customerId = event.path("data").path("customerId").asText();
            boolean approved  = event.path("data").path("approved").asBoolean();
            log.info("KYC event: customerId={}, approved={}", customerId, approved);
            // TODO: unlock requiresKyc=true services for this customer
        } catch (Exception e) {
            log.error("Error procesando KYC event: {}", e.getMessage(), e);
        }
    }
}
