package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.LoanCategory;
import com.magenta.banks.domain.model.Scheme;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.model.pagination.PageResult;
import com.magenta.banks.domain.model.pagination.PageSpec;
import com.magenta.banks.domain.port.out.LoanProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SearchProductsUseCase {

    private final LoanProductRepository productRepository;

    public SearchProductsUseCase(LoanProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public record Query(
        UUID tenantId,
        Scheme scheme,
        BigDecimal ltvMin,
        Integer maxAge,
        BigDecimal ticketAmount,
        LoanCategory category,
        PageSpec page
    ) {}

    @Transactional(readOnly = true)
    public PageResult<LoanProduct> execute(Query q) {
        return productRepository.search(
            q.tenantId(), q.scheme(), q.ltvMin(),
            q.maxAge(), q.ticketAmount(), q.category(), q.page()
        );
    }

    @Transactional(readOnly = true)
    public List<LoanProduct> findByBankId(UUID bankId) {
        return productRepository.findActiveByBankId(bankId);
    }

    @Transactional(readOnly = true)
    public LoanProduct findById(UUID id) {
        return productRepository.findById(id)
               .orElseThrow(() -> new NoSuchElementException("LoanProduct not found: " + id));
    }
}
