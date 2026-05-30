package com.magenta.banks.infrastructure.adapter.out.persistence.loanproduct;

import com.magenta.banks.domain.model.LoanCategory;
import com.magenta.banks.domain.model.Scheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LoanProductJpaRepository extends JpaRepository<LoanProductJpaEntity, UUID> {

    List<LoanProductJpaEntity> findByBankIdAndStatus(UUID bankId, String status);

    @Query("""
        SELECT p FROM LoanProductJpaEntity p
        WHERE p.tenantId = :tenantId
          AND p.status = 'ACTIVE'
          AND (:scheme IS NULL OR p.scheme = :schemeStr)
          AND (:ltvMin IS NULL OR p.ltvMaxPct >= :ltvMin)
          AND (:ticketAmount IS NULL OR (p.ticketMin <= :ticketAmount AND p.ticketMax >= :ticketAmount))
          AND (:category IS NULL OR p.category = :categoryStr)
        """)
    Page<LoanProductJpaEntity> search(
        @Param("tenantId")     UUID tenantId,
        @Param("scheme")       Scheme scheme,
        @Param("schemeStr")    String schemeStr,
        @Param("ltvMin")       BigDecimal ltvMin,
        @Param("ticketAmount") BigDecimal ticketAmount,
        @Param("category")     LoanCategory category,
        @Param("categoryStr")  String categoryStr,
        Pageable pageable
    );

    List<LoanProductJpaEntity> findByIdIn(List<UUID> ids);
}
