package com.magenta.products.application;

import com.magenta.products.domain.model.Property;
import com.magenta.products.domain.port.out.DomainEventPublisher;
import com.magenta.products.domain.port.out.PropertyRepository;
import com.magenta.products.domain.port.out.SearchIndexPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Archivado de inmueble.
 */
@Service
@Transactional
public class ArchivePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final SearchIndexPort searchIndexPort;
    private final DomainEventPublisher eventPublisher;

    public ArchivePropertyUseCase(PropertyRepository propertyRepository,
                                   SearchIndexPort searchIndexPort,
                                   DomainEventPublisher eventPublisher) {
        this.propertyRepository = propertyRepository;
        this.searchIndexPort = searchIndexPort;
        this.eventPublisher = eventPublisher;
    }

    public Property execute(UUID propertyId, String archivedBy) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));
        property.archive(archivedBy);
        Property saved = propertyRepository.save(property);
        searchIndexPort.delete(propertyId);
        saved.pullDomainEvents().forEach(eventPublisher::publish);
        return saved;
    }
}
