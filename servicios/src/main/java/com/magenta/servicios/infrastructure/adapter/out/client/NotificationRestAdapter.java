package com.magenta.servicios.infrastructure.adapter.out.client;

import com.magenta.servicios.domain.port.out.NotificationPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

/**
 * Adaptador REST hacia el servicio de notificaciones de la plataforma Magenta.
 *
 * <p>Envía notificaciones push/email/SMS delegando en el microservicio
 * {@code notifications} (puerto 8086 por convención).
 *
 * <p>Resilience4j: circuit-breaker + retry sólo en operaciones idempotentes.
 * Si el servicio de notificaciones no está disponible, se loguea el error
 * pero NO se propaga la excepción — una notificación fallida no debe
 * revertir la transacción de negocio que la origina.
 */
@Component
public class NotificationRestAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationRestAdapter.class);

    private final RestClient restClient;

    public NotificationRestAdapter(
            @Value("${magenta.clients.notifications:http://notifications:8086}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "notifications", fallbackMethod = "logOrderStatusFallback")
    @Retry(name = "notifications")
    public void sendOrderStatusUpdate(UUID customerId, UUID orderId, String status, String message) {
        restClient.post()
                .uri("/api/v1/notifications/order-status")
                .body(Map.of(
                        "customerId", customerId,
                        "orderId",    orderId,
                        "status",     status,
                        "message",    message
                ))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    @CircuitBreaker(name = "notifications", fallbackMethod = "logSlaWarningFallback")
    @Retry(name = "notifications")
    public void sendSlaWarning(UUID customerId, UUID orderId, String details) {
        restClient.post()
                .uri("/api/v1/notifications/sla-warning")
                .body(Map.of(
                        "customerId", customerId,
                        "orderId",    orderId,
                        "details",    details
                ))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    @CircuitBreaker(name = "notifications", fallbackMethod = "logDeliverableFallback")
    @Retry(name = "notifications")
    public void sendDeliverableReady(UUID customerId, UUID orderId, UUID deliverableId) {
        restClient.post()
                .uri("/api/v1/notifications/deliverable-ready")
                .body(Map.of(
                        "customerId",    customerId,
                        "orderId",       orderId,
                        "deliverableId", deliverableId
                ))
                .retrieve()
                .toBodilessEntity();
    }

    // ── Fallbacks (degradación controlada: loguear, no fallar el caller) ──────

    public void logOrderStatusFallback(UUID customerId, UUID orderId, String status,
                                       String message, Throwable t) {
        log.warn("Notifications CB open — order-status NOT sent: customerId={} orderId={} status={}",
                customerId, orderId, status);
    }

    public void logSlaWarningFallback(UUID customerId, UUID orderId, String details, Throwable t) {
        log.warn("Notifications CB open — sla-warning NOT sent: customerId={} orderId={}",
                customerId, orderId);
    }

    public void logDeliverableFallback(UUID customerId, UUID orderId, UUID deliverableId, Throwable t) {
        log.warn("Notifications CB open — deliverable-ready NOT sent: customerId={} orderId={} deliverableId={}",
                customerId, orderId, deliverableId);
    }
}
