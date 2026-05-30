package com.magenta.servicios.infrastructure.adapter.in.camunda;

import com.magenta.servicios.application.usecase.QuoteServiceUseCase;
import com.magenta.servicios.domain.model.ServiceCode;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class QuotePriceWorker {

    private static final Logger log = LoggerFactory.getLogger(QuotePriceWorker.class);

    private final QuoteServiceUseCase quoteUseCase;

    public QuotePriceWorker(QuoteServiceUseCase quoteUseCase) {
        this.quoteUseCase = quoteUseCase;
    }

    @JobWorker(type = "quotePrice")
    public Map<String, Object> quotePrice(@Variable String orderId,
                                          @Variable String serviceCode,
                                          @Variable(required = false) String propertyId,
                                          @Variable(required = false) String customerId) {
        log.info("Calculando precio para orden {}", orderId);
        try {
            UUID propId = propertyId != null ? UUID.fromString(propertyId) : null;
            UUID custId = customerId != null ? UUID.fromString(customerId) : null;
            QuoteServiceUseCase.QuoteResult result = quoteUseCase.execute(
                    ServiceCode.valueOf(serviceCode), custId, propId, null, null);
            return Map.of("priceQuoted", result.priceQuoted(), "currency", result.currency());
        } catch (Exception e) {
            log.error("Error cotizando: {}", e.getMessage(), e);
            return Map.of("priceQuoted", 0, "quotingError", e.getMessage());
        }
    }
}
