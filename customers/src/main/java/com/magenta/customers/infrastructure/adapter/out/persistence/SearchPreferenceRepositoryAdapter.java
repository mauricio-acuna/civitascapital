package com.magenta.customers.infrastructure.adapter.out.persistence;

import com.magenta.customers.domain.model.SearchPreference;
import com.magenta.customers.domain.port.out.SearchPreferenceRepository;
import com.magenta.customers.infrastructure.adapter.out.persistence.jpa.JpaSearchPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchPreferenceRepositoryAdapter implements SearchPreferenceRepository {

    private final JpaSearchPreferenceRepository jpa;
    private final CustomerPersistenceMapper mapper;

    @Override
    public SearchPreference save(SearchPreference preference) {
        var entity = mapper.toSearchPreferenceEntity(preference);
        return mapper.toSearchPreference(jpa.save(entity));
    }

    @Override
    public Optional<SearchPreference> findById(UUID id) {
        return jpa.findById(id).map(mapper::toSearchPreference);
    }

    @Override
    public List<SearchPreference> findActiveByCustomerId(UUID customerId) {
        return jpa.findByCustomerIdAndActiveTrue(customerId).stream()
                  .map(mapper::toSearchPreference)
                  .collect(Collectors.toList());
    }

    @Override
    public List<SearchPreference> findAllActiveWithAlerts() {
        return jpa.findByActiveTrueAndAlertChannelNot("NONE").stream()
                  .map(mapper::toSearchPreference)
                  .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
    }
}
