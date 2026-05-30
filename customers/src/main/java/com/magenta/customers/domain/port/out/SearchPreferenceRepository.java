package com.magenta.customers.domain.port.out;

import com.magenta.customers.domain.model.SearchPreference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SearchPreferenceRepository {
    SearchPreference save(SearchPreference preference);
    Optional<SearchPreference> findById(UUID id);
    List<SearchPreference> findActiveByCustomerId(UUID customerId);
    List<SearchPreference> findAllActiveWithAlerts();
    void delete(UUID id);
}
