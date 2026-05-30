package com.magenta.servicios.infrastructure.adapter.in.camunda;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ValidateInputsWorker {

    private static final Logger log = LoggerFactory.getLogger(ValidateInputsWorker.class);

    @JobWorker(type = "validateInputs")
    public Map<String, Object> validateInputs(@Variable String orderId,
                                               @Variable String serviceCode,
                                               @Variable(name = "inputs") String inputsJson) {
        log.info("Validando inputs para orden {} servicio {}", orderId, serviceCode);
        // TODO: validate against JSON Schema from ServiceDefinition
        return Map.of("inputsValid", true, "validationMessage", "OK");
    }
}
