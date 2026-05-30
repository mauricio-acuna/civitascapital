package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.event.SlaBreachedEvent;
import com.magenta.servicios.domain.model.ServiceOrder;
import com.magenta.servicios.domain.port.out.OutboxEventPublisher;
import com.magenta.servicios.domain.port.out.ServiceOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SlaMonitorScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaMonitorScheduler.class);

    private final ServiceOrderRepository orders;
    private final OutboxEventPublisher outbox;

    public SlaMonitorScheduler(ServiceOrderRepository orders, OutboxEventPublisher outbox) {
        this.orders = orders;
        this.outbox = outbox;
    }

    @Scheduled(fixedRateString = "${magenta.sla.monitor.rate-ms:60000}")
    @Transactional
    public void checkOverdueSla() {
        List<ServiceOrder> overdueOrders = orders.findOverdueSlaOrders(Instant.now());

        for (ServiceOrder order : overdueOrders) {
            log.warn("SLA vencido para orden {}, due={}", order.getId(), order.getSlaDueAt());
            outbox.publish("magenta.servicios.sla.v1",
                    new SlaBreachedEvent(order.getId(), order.getTenantId(),
                            order.getCustomerId(), order.getSlaDueAt()));
        }
    }
}
