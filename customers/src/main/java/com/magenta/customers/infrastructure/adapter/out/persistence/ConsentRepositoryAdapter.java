package com.magenta.customers.infrastructure.adapter.out.persistence;

import com.magenta.customers.domain.model.Consent;
import com.magenta.customers.domain.port.out.ConsentRepository;
import com.magenta.customers.infrastructure.adapter.out.persistence.jpa.JpaConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConsentRepositoryAdapter implements ConsentRepository {

    private final JpaConsentRepository jpa;
    private final CustomerPersistenceMapper mapper;

    @Override
    public Consent save(Consent consent) {
        var entity = mapper.toConsentEntity(consent);
        return mapper.toConsent(jpa.save(entity));
    }

    @Override
    public Optional<Consent> findByCustomerIdAndPurpose(UUID customerId, String purpose) {
        return jpa.findByCustomerIdAndPurpose(customerId, purpose)
                  .map(mapper::toConsent);
    }

    @Override
    public List<Consent> findByCustomerId(UUID customerId) {
        return jpa.findByCustomerId(customerId).stream()
                  .map(mapper::toConsent)
                  .collect(Collectors.toList());
    }

    @Override
    public boolean hasActiveConsent(UUID customerId, String purpose) {
        return jpa.hasActiveConsent(customerId, purpose);
    }
}
