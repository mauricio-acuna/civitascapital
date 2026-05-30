package com.magenta.customers.infrastructure.adapter.out.persistence.jpa;

import com.magenta.customers.infrastructure.adapter.out.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface JpaCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    Optional<CustomerJpaEntity> findByKeycloakUserIdAndDeletedAtIsNull(String keycloakUserId);

    @Query("SELECT CASE WHEN COUNT(ip) > 0 THEN true ELSE false END " +
           "FROM IndividualProfileJpaEntity ip WHERE ip.nifHash = :hash")
    boolean existsByNifHash(@Param("hash") String hash);

    @Query("SELECT CASE WHEN COUNT(ip) > 0 THEN true ELSE false END " +
           "FROM IndividualProfileJpaEntity ip WHERE ip.emailHash = :hash")
    boolean existsByEmailHash(@Param("hash") String hash);

    @Modifying
    @Query("UPDATE CustomerJpaEntity c SET c.deletedAt = :now, c.updatedBy = :by, c.updatedAt = :now " +
           "WHERE c.id = :id AND c.deletedAt IS NULL")
    int softDelete(@Param("id") UUID id, @Param("now") Instant now, @Param("by") String by);
}
