package com.magenta.customers.domain.port.out;

import com.magenta.customers.domain.model.DocumentRef;
import com.magenta.customers.domain.model.ValidationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    DocumentRef save(DocumentRef document);
    Optional<DocumentRef> findById(UUID id);
    List<DocumentRef> findByCustomerId(UUID customerId);
    DocumentRef updateValidationStatus(UUID id, ValidationStatus status);
    /** Cuenta documentos verificados por tipo para el cálculo de confidence. */
    long countValidByCustomerId(UUID customerId);
}
