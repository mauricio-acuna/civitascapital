package com.magenta.servicios.domain.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGatewayPort {
    String createPaymentIntent(UUID orderId, BigDecimal amount, String currency, String customerId);
    void capturePayment(String providerRef);
    void refundPayment(String providerRef);
    String createPayout(UUID partnerId, BigDecimal amount, String ibanEncrypted, String reference);
}
