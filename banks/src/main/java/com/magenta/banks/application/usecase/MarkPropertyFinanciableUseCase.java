package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.Scheme;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.port.out.LoanProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * UC-B6: generar badge de financiabilidad para un inmueble sin perfil de cliente.
 *
 * Este flujo alimenta `products.financing`: permite mostrar que un inmueble tiene
 * productos bancarios potencialmente compatibles antes de abrir una simulacion
 * personalizada con datos del comprador.
 */
@Service
public class MarkPropertyFinanciableUseCase {

    private final LoanProductRepository productRepository;

    public MarkPropertyFinanciableUseCase(LoanProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public record Command(
            UUID tenantId,
            UUID propertyId,
            BigDecimal propertyPrice
    ) {}

    public record Result(
            UUID propertyId,
            List<UUID> feasibleBankProductIds,
            UUID best90_5_5ProductId,
            boolean has90_5_5
    ) {}

    @Transactional(readOnly = true)
    public Result execute(Command command) {
        if (command.propertyPrice() == null || command.propertyPrice().signum() <= 0) {
            throw new IllegalArgumentException("propertyPrice must be positive");
        }

        List<LoanProduct> products = productRepository
                .search(command.tenantId(), null, null, null, command.propertyPrice(), null, Pageable.ofSize(100))
                .getContent()
                .stream()
                .filter(LoanProduct::isActive)
                .toList();

        List<UUID> ids = products.stream().map(LoanProduct::id).toList();
        UUID bestNinetyFiveFive = products.stream()
                .filter(product -> product.scheme() == Scheme.NINETY_FIVE_FIVE)
                .map(LoanProduct::id)
                .findFirst()
                .orElse(null);

        return new Result(command.propertyId(), ids, bestNinetyFiveFive, bestNinetyFiveFive != null);
    }
}

