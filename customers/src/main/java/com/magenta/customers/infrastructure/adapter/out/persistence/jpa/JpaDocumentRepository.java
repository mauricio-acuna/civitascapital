package com.magenta.customers.infrastructure.adapter.out.persistence.jpa;

import com.magenta.customers.infrastructure.adapter.out.persistence.entity.DocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaDocumentRepository extends JpaRepository<DocumentJpaEntity, UUID> {

    List<DocumentJpaEntity> findByCustomerIdOrderByUploadedAtDesc(UUID customerId);

    @Query("SELECT COUNT(d) FROM DocumentJpaEntity d WHERE d.customer.id = :cid AND d.validationStatus = 'VALID'")
    long countValidByCustomerId(@Param("cid") UUID customerId);
}
