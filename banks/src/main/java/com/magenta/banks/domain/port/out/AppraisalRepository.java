package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.model.appraisal.Appraisal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppraisalRepository {
    Appraisal save(Appraisal appraisal);
    Optional<Appraisal> findById(UUID id);
    List<Appraisal> findByPropertyId(UUID propertyId);
    List<Appraisal> findByCustomerId(UUID customerId);
    Optional<Appraisal> findLatestValidByPropertyId(UUID propertyId);
}
