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
public class PayoutPartnerWorker {

    private static final Logger log = LoggerFactory.getLogger(PayoutPartnerWorker.class);
    private final PaymentGatewayPort paymentGateway;

    public PayoutPartnerWorker(PaymentGatewayPort paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @JobWorker(type = "payoutPartner")
    public Map<String, Object> payoutPartner(@Variable String orderId,
                                              @Variable String partnerId,
                                              @Variable BigDecimal partnerAmount,
                                              @Variable(required = false) String stripeAccountId) {
        log.info("Pagando a partner {} para orden {}", partnerId, orderId);
        try {
            String transferId = paymentGateway.createPayout(
                    UUID.fromString(partnerId), partnerAmount, null,
                    stripeAccountId != null ? stripeAccountId : partnerId);
            return Map.of("payoutRef", transferId, "payoutStatus", "SENT");
        } catch (Exception e) {
            log.error("Error en payout a partner: {}", e.getMessage(), e);
            return Map.of("payoutStatus", "FAILED", "payoutError", e.getMessage());
        }
    }
}
