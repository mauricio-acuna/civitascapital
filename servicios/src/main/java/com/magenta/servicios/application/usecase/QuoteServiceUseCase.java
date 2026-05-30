package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.model.*;
import com.magenta.servicios.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class QuoteServiceUseCase {

    private final ServiceCatalogRepository catalog;
    private final ProductClientPort productClient;

    public QuoteServiceUseCase(ServiceCatalogRepository catalog,
                               ProductClientPort productClient) {
        this.catalog = catalog;
        this.productClient = productClient;
    }

    public QuoteResult execute(ServiceCode code, UUID customerId, UUID propertyId,
                               UUID operationId, String extraInputsJson) {
        ServiceDefinition definition = catalog.findByCode(code)
                .filter(ServiceDefinition::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + code));

        BigDecimal price = computePrice(definition, propertyId, extraInputsJson);
        Instant validUntil = Instant.now().plus(1, ChronoUnit.DAYS);

        return new QuoteResult(code, price, "EUR", validUntil,
                List.of(new QuoteBreakdown("Precio base", price)),
                definition.getSlaHours());
    }

    private BigDecimal computePrice(ServiceDefinition def, UUID propertyId, String extraInputs) {
        return switch (def.getPricingModel()) {
            case FIXED -> def.getBasePrice() != null ? def.getBasePrice() : BigDecimal.ZERO;
            case PERCENT_OF_PRICE -> {
                if (propertyId != null && def.getPriceFormula() != null) {
                    ProductClientPort.PropertyData prop = productClient.getProperty(propertyId);
                    // Simple: extract coefficient from formula "X * property.price"
                    String formula = def.getPriceFormula();
                    double coef = Double.parseDouble(formula.split("\\*")[0].trim());
                    yield prop.price().multiply(BigDecimal.valueOf(coef)).setScale(2, java.math.RoundingMode.HALF_UP);
                }
                yield BigDecimal.ZERO;
            }
            case MONTHLY_SUBSCRIPTION -> def.getBasePrice() != null ? def.getBasePrice() : BigDecimal.ZERO;
            case QUOTE_BASED -> BigDecimal.ZERO; // precio se determina manualmente
        };
    }

    public record QuoteResult(ServiceCode serviceCode, BigDecimal priceQuoted, String currency,
                               Instant validUntil, List<QuoteBreakdown> breakdown, int slaHours) {}

    public record QuoteBreakdown(String concept, BigDecimal amount) {}
}
