package com.magenta.areas.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.areas.domain.model.ZoneDemandSnapshot;
import com.magenta.areas.domain.port.out.DemandRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consume eventos de servicios/workflow e incrementa leads/searches en ZoneDemandSnapshot.
 */
@Component
public class ServiciosWorkflowConsumer {

    private static final Logger log = LoggerFactory.getLogger(ServiciosWorkflowConsumer.class);

    private final DemandRepositoryPort demandRepo;
    private final ProcessedEventRepository processedEventRepo;
    private final ObjectMapper objectMapper;

    public ServiciosWorkflowConsumer(DemandRepositoryPort demandRepo,
                                      ProcessedEventRepository processedEventRepo,
                                      ObjectMapper objectMapper) {
        this.demandRepo         = demandRepo;
        this.processedEventRepo = processedEventRepo;
        this.objectMapper       = objectMapper;
    }

    @KafkaListener(topics = "magenta.servicios.workflow.v1",
                   groupId = "areas-servicios-consumer",
                   containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consume(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            JsonNode envelope = objectMapper.readTree(message);
            String eventId = envelope.path("id").asText();

            if (processedEventRepo.isAlreadyProcessed("areas-servicios-consumer", "areas-servicios-consumer", eventId)) {
                return;
            }

            JsonNode data = envelope.path("data");
            UUID zoneId = UUID.fromString(data.path("zoneId").asText());
            String eventType = envelope.path("type").asText();
            LocalDate period = LocalDate.now().withDayOfMonth(1);

            ZoneDemandSnapshot snapshot = demandRepo.findByZoneAndPeriod(zoneId, period)
                    .orElse(ZoneDemandSnapshot.reconstitute(
                            UUID.randomUUID(), zoneId, period, 0, 0, 0, 0, null));

            if (eventType.contains("SearchCreated")) {
                snapshot.incrementSearches(1);
            } else if (eventType.contains("LeadCreated")) {
                snapshot.incrementLeads(1);
            }

            demandRepo.save(snapshot);
            processedEventRepo.markProcessed("areas-servicios-consumer", "areas-servicios-consumer", eventId);

        } catch (Exception ex) {
            log.error("Error processing servicios workflow event key={}: {}", key, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
}
