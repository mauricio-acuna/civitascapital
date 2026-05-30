package com.magenta.customers.infrastructure.adapter.out.persistence;

import com.magenta.customers.domain.model.DocumentRef;
import com.magenta.customers.domain.model.ValidationStatus;
import com.magenta.customers.domain.port.out.DocumentRepository;
import com.magenta.customers.infrastructure.adapter.out.persistence.jpa.JpaDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentRepositoryAdapter implements DocumentRepository {

    private final JpaDocumentRepository jpa;
    private final CustomerPersistenceMapper mapper;

    @Override
    public DocumentRef save(DocumentRef doc) {
        var entity = mapper.toDocumentEntity(doc);
        return mapper.toDocumentRef(jpa.save(entity));
    }

    @Override
    public Optional<DocumentRef> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDocumentRef);
    }

    @Override
    public List<DocumentRef> findByCustomerId(UUID customerId) {
        return jpa.findByCustomerId(customerId).stream()
                  .map(mapper::toDocumentRef)
                  .collect(Collectors.toList());
    }

    @Override
    public void updateValidationStatus(UUID id, ValidationStatus status) {
        jpa.findById(id).ifPresent(e -> {
            e.setValidationStatus(status.name());
            jpa.save(e);
        });
    }

    @Override
    public long countValidByCustomerId(UUID customerId) {
        return jpa.countValidByCustomerId(customerId);
    }
}
