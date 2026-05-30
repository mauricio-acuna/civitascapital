package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.Visit;

import java.util.Optional;
import java.util.UUID;

public interface VisitRepository {
    Visit save(Visit visit);
    Optional<Visit> findById(UUID id);
}
