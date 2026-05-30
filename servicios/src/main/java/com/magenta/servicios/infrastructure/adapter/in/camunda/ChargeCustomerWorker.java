package com.magenta.servicios.infrastructure.adapter.in.camunda;

import com.magenta.servicios.domain.port.out.PaymentGatewayPort;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class ChargeCustomerWorker {

    private static final Logger log = LoggerFactory.getLogger(ChargeCustomerWorker.class);
    private final PaymentGatewayPort paymentGateway;

    public ChargeCustomerWorker(PaymentGatewayPort paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @JobWorker(type = "chargeCustomer")
    public Map<String, Object> chargeCustomer(@Variable String orderId,
                                               @Variable String customerId,
                                               @Variable BigDecimal priceQuoted,
                                               @Variable(required = false) String currency) {
        log.info("Cobrando al cliente {} para orden {}", customerId, orderId);
        String curr = currency != null ? currency : "EUR";
        try {
            String paymentIntentId = paymentGateway.createPaymentIntent(
                    UUID.fromString(orderId), priceQuoted, curr, customerId);
            paymentGateway.capturePayment(paymentIntentId);
            return Map.of("paymentRef", paymentIntentId, "paymentStatus", "CAPTURED");
        } catch (Exception e) {
            log.error("Error cobrando cliente: {}", e.getMessage(), e);
            return Map.of("paymentStatus", "FAILED", "paymentError", e.getMessage());
        }
    }
}
