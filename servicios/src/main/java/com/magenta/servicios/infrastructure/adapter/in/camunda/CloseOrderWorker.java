package com.magenta.servicios.infrastructure.adapter.in.camunda;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CloseOrderWorker {

    private static final Logger log = LoggerFactory.getLogger(CloseOrderWorker.class);

    @JobWorker(type = "closeOrder")
    public Map<String, Object> closeOrder(@Variable String orderId,
                                           @Variable(required = false) String finalStatus) {
        log.info("Cerrando orden {} con estado {}", orderId, finalStatus);
        // TODO: call AcceptOrderUseCase / set order COMPLETED
        return Map.of("orderClosed", true, "finalStatus",
                finalStatus != null ? finalStatus : "COMPLETED");
    }
}
