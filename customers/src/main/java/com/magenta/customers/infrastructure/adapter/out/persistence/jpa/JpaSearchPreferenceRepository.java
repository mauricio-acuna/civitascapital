package com.magenta.customers.infrastructure.adapter.out.persistence.jpa;

import com.magenta.customers.infrastructure.adapter.out.persistence.entity.SearchPreferenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaSearchPreferenceRepository extends JpaRepository<SearchPreferenceJpaEntity, UUID> {

    List<SearchPreferenceJpaEntity> findByCustomerIdAndActiveTrue(UUID customerId);

    List<SearchPreferenceJpaEntity> findByActiveTrueAndAlertChannelNot(String noneChannel);
}
