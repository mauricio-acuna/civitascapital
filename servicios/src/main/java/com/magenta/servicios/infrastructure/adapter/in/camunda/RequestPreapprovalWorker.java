package com.magenta.servicios.infrastructure.adapter.in.camunda;

import com.magenta.servicios.domain.port.out.BankClientPort;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Solicita al servicio de bancos una pre-aprobación hipotecaria.
 * Job type: {@code requestPreapproval} (referenciado en first-home-aid.bpmn, mortgage-broker.bpmn).
 *
 * <p>Variables de entrada:
 * <ul>
 *   <li>{@code orderId} — UUID de la orden de servicio</li>
 *   <li>{@code customerId} — UUID del cliente</li>
 *   <li>{@code propertyId} — UUID de la propiedad</li>
 *   <li>{@code amount} — importe solicitado</li>
 * </ul>
 *
 * <p>Variables de salida:
 * <ul>
 *   <li>{@code preapprovalRef} — referencia devuelta por el banco</li>
 *   <li>{@code preapprovalStatus} — REQUESTED | FAILED</li>
 * </ul>
 */
@Component
public class RequestPreapprovalWorker {

    private static final Logger log = LoggerFactory.getLogger(RequestPreapprovalWorker.class);

    private final BankClientPort bankClient;

    public RequestPreapprovalWorker(BankClientPort bankClient) {
        this.bankClient = bankClient;
    }

    @JobWorker(type = "requestPreapproval")
    public Map<String, Object> requestPreapproval(@Variable String orderId,
                                                   @Variable String customerId,
                                                   @Variable String propertyId,
                                                   @Variable BigDecimal amount) {
        log.info("Solicitando pre-aprobación: orderId={} customerId={} propertyId={} amount={}",
                orderId, customerId, propertyId, amount);
        try {
            String ref = bankClient.createPreapproval(
                    UUID.fromString(customerId),
                    UUID.fromString(propertyId),
                    amount);
            log.info("Pre-aprobación concedida: orderId={} ref={}", orderId, ref);
            return Map.of("preapprovalRef", ref, "preapprovalStatus", "REQUESTED");
        } catch (Exception e) {
            log.error("Error solicitando pre-aprobación para orden {}: {}", orderId, e.getMessage(), e);
            return Map.of("preapprovalStatus", "FAILED", "preapprovalError", e.getMessage());
        }
    }
}
