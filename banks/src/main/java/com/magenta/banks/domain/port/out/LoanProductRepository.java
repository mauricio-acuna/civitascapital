package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.model.LoanCategory;
import com.magenta.banks.domain.model.Scheme;
import com.magenta.banks.domain.model.pagination.PageResult;
import com.magenta.banks.domain.model.pagination.PageSpec;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanProductRepository {
    LoanProduct save(LoanProduct product);
    Optional<LoanProduct> findById(UUID id);
    List<LoanProduct> findActiveByBankId(UUID bankId);

    PageResult<LoanProduct> search(
        UUID tenantId,
        Scheme scheme,
        BigDecimal ltvMin,
        Integer maxAge,
        BigDecimal ticketAmount,
        LoanCategory category,
        PageSpec page
    );

    List<LoanProduct> findActiveByIds(List<UUID> ids);
}
