package com.magenta.products.application;

import com.magenta.products.domain.model.*;
import com.magenta.products.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * UC-P2: Publicar operación con precio y condiciones + validación de invariantes.
 *
 * Diseño transaccional (alineado con stack-tech_spec §6.5):
 * Las llamadas HTTP a servicios externos (banks/financing) se realizan ANTES
 * de abrir la transacción PostgreSQL. La transacción sólo encierra operaciones
 * sobre la base de datos local, garantizando que nunca haya una conexión de BD
 * abierta mientras se espera respuesta de un servicio externo.
 */
@Service
public class PublishPropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final FinancingPort financingPort;
    private final DomainEventPublisher eventPublisher;
    private final SearchIndexPort searchIndexPort;

    public PublishPropertyUseCase(PropertyRepository propertyRepository,
                                   FinancingPort financingPort,
                                   DomainEventPublisher eventPublisher,
                                   SearchIndexPort searchIndexPort) {
        this.propertyRepository = propertyRepository;
        this.financingPort = financingPort;
        this.eventPublisher = eventPublisher;
        this.searchIndexPort = searchIndexPort;
    }

    public Property execute(UUID propertyId, String publishedBy) {
        // ── Phase 1: read (short, separate tx) ──────────────────────────────
        Property property = loadProperty(propertyId);

        // ── Phase 2: HTTP calls OUTSIDE transaction ──────────────────────────
        // spec §6.5: never call external HTTP inside an open DB transaction
        FinancingHint financingHint = evaluateFinancingOutsideTransaction(property);

        // ── Phase 3: write (single tx — only local DB ops) ──────────────────
        return publishInTransaction(propertyId, publishedBy, financingHint);
    }

    @Transactional(readOnly = true)
    protected Property loadProperty(UUID propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));
    }

    private FinancingHint evaluateFinancingOutsideTransaction(Property property) {
        Optional<Operation> activeOp = property.operations().stream()
                .filter(o -> o.status() == OperationStatus.ACTIVE
                          || o.status() == OperationStatus.DRAFT)
                .findFirst();

        if (activeOp.isEmpty()) return FinancingHint.empty();

        BigDecimal price = activeOp.get().price().amount();
        return financingPort.evaluateFeasibility(property.id(), price);
    }

    @Transactional
    protected Property publishInTransaction(UUID propertyId, String publishedBy,
                                             FinancingHint financingHint) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));

        if (financingHint != null) {
            property.updateFinancing(financingHint);
        }

        property.publish(publishedBy);
        Property saved = propertyRepository.save(property);

        // Index outside tx would be ideal; here we accept best-effort on commit
        searchIndexPort.index(saved);
        saved.pullDomainEvents().forEach(eventPublisher::publish);
        return saved;
    }
}
