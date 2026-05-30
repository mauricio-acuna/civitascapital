package com.magenta.servicios.domain.port.out;

import java.util.UUID;

public interface NotificationPort {
    void sendOrderStatusUpdate(UUID customerId, UUID orderId, String status, String message);
    void sendSlaWarning(UUID customerId, UUID orderId, String details);
    void sendDeliverableReady(UUID customerId, UUID orderId, UUID deliverableId);
}
