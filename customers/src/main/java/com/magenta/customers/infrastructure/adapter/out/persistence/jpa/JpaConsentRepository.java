package com.magenta.customers.infrastructure.adapter.out.persistence.jpa;

import com.magenta.customers.infrastructure.adapter.out.persistence.entity.ConsentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaConsentRepository extends JpaRepository<ConsentJpaEntity, UUID> {

    Optional<ConsentJpaEntity> findByCustomerIdAndPurpose(UUID customerId, String purpose);

    List<ConsentJpaEntity> findByCustomerId(UUID customerId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ConsentJpaEntity c " +
           "WHERE c.customer.id = :cid AND c.purpose = :purpose AND c.granted = true AND c.revokedAt IS NULL")
    boolean hasActiveConsent(@Param("cid") UUID customerId, @Param("purpose") String purpose);
}
