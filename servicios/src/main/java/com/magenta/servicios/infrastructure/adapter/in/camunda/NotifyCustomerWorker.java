package com.magenta.servicios.infrastructure.adapter.in.camunda;

import com.magenta.servicios.domain.port.out.NotificationPort;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NotifyCustomerWorker {

    private static final Logger log = LoggerFactory.getLogger(NotifyCustomerWorker.class);
    private final NotificationPort notificationPort;

    public NotifyCustomerWorker(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    @JobWorker(type = "notifyCustomer")
    public Map<String, Object> notifyCustomer(@Variable String orderId,
                                               @Variable String customerId,
                                               @Variable String notificationType,
                                               @Variable(required = false) String message) {
        log.info("Notificando al cliente {} sobre orden {}", customerId, orderId);
        notificationPort.sendOrderStatusUpdate(
                UUID.fromString(customerId), UUID.fromString(orderId),
                notificationType, message != null ? message : "Actualización de tu servicio");
        return Map.of("notified", true);
    }
}
