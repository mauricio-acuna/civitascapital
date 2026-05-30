package com.magenta.servicios.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceDefinitionJpaRepository extends JpaRepository<ServiceDefinitionJpaEntity, UUID> {
    Optional<ServiceDefinitionJpaEntity> findByCode(String code);

    @Query("SELECT s FROM ServiceDefinitionJpaEntity s WHERE s.status = 'ACTIVE'")
    List<ServiceDefinitionJpaEntity> findAllActive();
}
