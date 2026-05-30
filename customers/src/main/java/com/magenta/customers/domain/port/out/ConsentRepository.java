package com.magenta.customers.domain.port.out;

import com.magenta.customers.domain.model.Consent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRepository {
    Consent save(Consent consent);
    Optional<Consent> findByCustomerIdAndPurpose(UUID customerId, String purpose);
    List<Consent> findByCustomerId(UUID customerId);
    boolean hasActiveConsent(UUID customerId, String purpose);
}
