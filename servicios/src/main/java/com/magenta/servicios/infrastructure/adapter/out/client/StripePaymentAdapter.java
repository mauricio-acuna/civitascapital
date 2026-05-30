package com.magenta.servicios.infrastructure.adapter.out.client;

import com.magenta.servicios.domain.port.out.PaymentGatewayPort;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.Transfer;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.TransferCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class StripePaymentAdapter implements PaymentGatewayPort {

    public StripePaymentAdapter(@Value("${magenta.stripe.api-key}") String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @Override
    public String createPaymentIntent(UUID orderId, BigDecimal amount, String currency,
                                       String customerId) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(currency.toLowerCase())
                    .setCustomer(customerId)
                    .putMetadata("orderId", orderId.toString())
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Error creando PaymentIntent: " + e.getMessage(), e);
        }
    }

    @Override
    public void capturePayment(String providerRef) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(providerRef);
            intent.capture();
        } catch (StripeException e) {
            throw new RuntimeException("Error capturando pago: " + e.getMessage(), e);
        }
    }

    @Override
    public void refundPayment(String providerRef) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(providerRef)
                    .build();
            Refund.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Error realizando reembolso: " + e.getMessage(), e);
        }
    }

    @Override
    public String createPayout(UUID partnerId, BigDecimal amount, String ibanEncrypted,
                                String reference) {
        try {
            // Stripe Connect: transfer to connected account
            TransferCreateParams params = TransferCreateParams.builder()
                    .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("eur")
                    .setDestination(reference) // Stripe connected account ID
                    .putMetadata("partnerId", partnerId.toString())
                    .build();
            Transfer transfer = Transfer.create(params);
            return transfer.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Error creando payout a partner: " + e.getMessage(), e);
        }
    }
}
