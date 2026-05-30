package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.Lead;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadRepository {
    Lead save(Lead lead);
    Optional<Lead> findById(UUID id);
    List<Lead> findByPropertyId(UUID propertyId);
}
