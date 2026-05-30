package com.magenta.servicios.domain.port.out;

import com.magenta.servicios.domain.model.Deliverable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliverableRepository {
    Deliverable save(Deliverable deliverable);
    Optional<Deliverable> findById(UUID id);
    List<Deliverable> findByOrderId(UUID orderId);
}
