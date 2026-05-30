package com.magenta.banks.infrastructure.adapter.out.persistence.bank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BankJpaRepository extends JpaRepository<BankJpaEntity, UUID> {

    @Query("SELECT b FROM BankJpaEntity b WHERE b.tenantId = :tenantId AND b.active = true AND b.deletedAt IS NULL")
    List<BankJpaEntity> findAllActiveByTenantId(@Param("tenantId") UUID tenantId);

    boolean existsByCode(String code);
}
