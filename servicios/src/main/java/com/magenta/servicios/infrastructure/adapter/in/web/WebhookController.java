package com.magenta.servicios.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Recibe webhooks de Stripe y de partners externos.
 * Verificación HMAC delegada al filtro WebhookSignatureFilter.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Recepción de callbacks externos")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @PostMapping("/stripe")
    @Operation(summary = "Webhook de Stripe (firma Stripe-Signature)")
    public ResponseEntity<Void> stripeWebhook(
            @RequestHeader("Stripe-Signature") String signature,
            @RequestBody String payload) {
        // HMAC verification is performed by WebhookSignatureFilter upstream
        log.info("Stripe webhook received, sig={}", signature.substring(0, 20));
        // Handled by StripeWebhookProcessor bean via ApplicationEvent
        return ResponseEntity.ok().build();
    }

    @PostMapping("/partners/{partnerCode}")
    @Operation(summary = "Webhook de partner externo (firma X-Partner-Signature)")
    public ResponseEntity<Void> partnerWebhook(
            @PathVariable String partnerCode,
            @RequestHeader("X-Partner-Signature") String signature,
            @RequestBody String payload) {
        log.info("Partner webhook received from {}", partnerCode);
        return ResponseEntity.ok().build();
    }
}
