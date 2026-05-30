package com.magenta.products.application;

import com.magenta.products.domain.model.*;
import com.magenta.products.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * UC-P8: Recibir feasibility de bancos y actualizar badge de financiación.
 *
 * HTTP call to banks performed OUTSIDE any DB transaction (spec §6.5).
 */
@Service
public class UpdateFinancingUseCase {

    private final PropertyRepository propertyRepository;
    private final FinancingPort financingPort;
    private final SearchIndexPort searchIndexPort;

    public UpdateFinancingUseCase(PropertyRepository propertyRepository,
                                   FinancingPort financingPort,
                                   SearchIndexPort searchIndexPort) {
        this.propertyRepository = propertyRepository;
        this.financingPort = financingPort;
        this.searchIndexPort = searchIndexPort;
    }

    public void execute(UUID propertyId) {
        // Phase 1: read (short read-only tx)
        Property property = loadProperty(propertyId);

        // Phase 2: HTTP call OUTSIDE transaction
        Optional<Operation> activeOp = property.operations().stream()
                .filter(o -> o.status() == OperationStatus.ACTIVE)
                .findFirst();

        if (activeOp.isEmpty()) return;

        BigDecimal price = activeOp.get().price().amount();
        FinancingHint hint = financingPort.evaluateFeasibility(propertyId, price);

        // Phase 3: write-only tx
        applyFinancingUpdate(propertyId, hint);
    }

    @Transactional(readOnly = true)
    protected Property loadProperty(UUID propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));
    }

    @Transactional
    protected void applyFinancingUpdate(UUID propertyId, FinancingHint hint) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));
        property.updateFinancing(hint);
        propertyRepository.save(property);
        if (property.status() == PropertyStatus.ACTIVE) {
            searchIndexPort.index(property);
        }
    }
}
