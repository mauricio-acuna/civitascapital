package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.model.ServiceDefinition;
import com.magenta.servicios.domain.model.ServiceCode;
import com.magenta.servicios.domain.port.out.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComparePackagesUseCase {

    private final ServiceCatalogRepository catalog;
    private final QuoteServiceUseCase quoteUseCase;

    public ComparePackagesUseCase(ServiceCatalogRepository catalog,
                                   QuoteServiceUseCase quoteUseCase) {
        this.catalog = catalog;
        this.quoteUseCase = quoteUseCase;
    }

    public List<PackageComparison> execute(List<ServiceCode> codes,
                                            java.util.UUID customerId,
                                            java.util.UUID propertyId) {
        return codes.stream()
                .map(code -> {
                    ServiceDefinition def = catalog.findByCode(code)
                            .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + code));
                    QuoteServiceUseCase.QuoteResult q = quoteUseCase.execute(
                            code, customerId, propertyId, null, null);
                    return new PackageComparison(code, def.getName(), q.priceQuoted(),
                            q.currency(), def.getSlaHours(), def.isRequiresKyc());
                })
                .toList();
    }

    public record PackageComparison(ServiceCode code, String name,
                                     java.math.BigDecimal price, String currency,
                                     int slaHours, boolean requiresKyc) {}
}
