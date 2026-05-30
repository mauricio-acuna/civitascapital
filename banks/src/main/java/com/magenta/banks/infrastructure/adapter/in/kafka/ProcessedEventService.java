package com.magenta.banks.infrastructure.adapter.in.kafka;

import com.magenta.banks.infrastructure.adapter.out.persistence.processed.ProcessedEventId;
import com.magenta.banks.infrastructure.adapter.out.persistence.processed.ProcessedEventJpaEntity;
import com.magenta.banks.infrastructure.adapter.out.persistence.processed.ProcessedEventJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProcessedEventService {

    private final ProcessedEventJpaRepository repository;

    public ProcessedEventService(ProcessedEventJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean markProcessingStarted(String consumerName, UUID eventId, String topic) {
        ProcessedEventId id = new ProcessedEventId(consumerName, eventId);
        if (repository.existsById(id)) {
            return false;
        }

        ProcessedEventJpaEntity entity = new ProcessedEventJpaEntity();
        entity.setId(id);
        entity.setTopic(topic);
        entity.setProcessedAt(Instant.now());
        try {
            repository.saveAndFlush(entity);
            return true;
        } catch (DataIntegrityViolationException duplicate) {
            return false;
        }
    }
}
