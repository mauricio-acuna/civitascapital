package com.magenta.servicios.infrastructure.config;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Verifica la firma HMAC de Stripe (header Stripe-Signature) y protección antirreplay
 * con ventana de 5 minutos (tolerancia por defecto del SDK de Stripe).
 */
@Component
public class StripeWebhookFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookFilter.class);

    @Value("${magenta.stripe.webhook-secret}")
    private String webhookSecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals("/api/v1/webhooks/stripe");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        String signature = request.getHeader("Stripe-Signature");

        if (signature == null) {
            log.warn("Stripe webhook sin cabecera Stripe-Signature");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Read body first
        filterChain.doFilter(wrappedRequest, response);
        String payload = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);

        try {
            // Validates signature AND timestamp within default 300s tolerance (antirreplay)
            Event.PRETTY_PRINT_GSON.toJson(
                    Webhook.constructEvent(payload, signature, webhookSecret));
        } catch (Exception e) {
            log.warn("Firma Stripe inválida: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
