package com.magenta.servicios.infrastructure.adapter.in.camunda;

import com.magenta.servicios.application.usecase.PartnerAssignmentService;
import com.magenta.servicios.domain.model.Partner;
import com.magenta.servicios.domain.model.ServiceCode;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class AssignPartnerWorker {

    private static final Logger log = LoggerFactory.getLogger(AssignPartnerWorker.class);
    private final PartnerAssignmentService assignmentService;

    public AssignPartnerWorker(PartnerAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @JobWorker(type = "assignPartner")
    public Map<String, Object> assignPartner(@Variable String orderId,
                                              @Variable String serviceCode,
                                              @Variable(required = false) String zoneId) {
        log.info("Asignando partner para orden {} servicio {}", orderId, serviceCode);
        UUID zone = zoneId != null ? UUID.fromString(zoneId) : UUID.randomUUID();
        Optional<Partner> partner = assignmentService.assign(ServiceCode.valueOf(serviceCode), zone);
        return partner
                .map(p -> Map.<String, Object>of("partnerId", p.getId().toString(), "partnerName", p.getName()))
                .orElseGet(() -> Map.of("partnerAssigned", false, "reason", "No partner available"));
    }
}
