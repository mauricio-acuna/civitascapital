package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.Operation;

import java.util.Optional;
import java.util.UUID;

public interface OperationRepository {
    Operation save(Operation operation);
    Optional<Operation> findById(UUID id);
}
