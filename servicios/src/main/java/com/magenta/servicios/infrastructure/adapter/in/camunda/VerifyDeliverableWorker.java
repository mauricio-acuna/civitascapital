package com.magenta.servicios.infrastructure.adapter.in.camunda;

import com.magenta.servicios.domain.model.Deliverable;
import com.magenta.servicios.domain.port.out.DeliverableRepository;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Verifica que un entregable existe, tiene el tipo correcto y su SHA-256 está presente.
 * Job type: {@code verifyDeliverable}.
 *
 * <p>Variables de entrada:
 * <ul>
 *   <li>{@code orderId} — UUID de la orden</li>
 *   <li>{@code deliverableId} — UUID del entregable a verificar</li>
 *   <li>{@code expectedKind} — opcional; si se indica, se valida que el kind coincida</li>
 * </ul>
 *
 * <p>Variables de salida:
 * <ul>
 *   <li>{@code deliverableVerified} — true / false</li>
 *   <li>{@code verificationReason} — descripción del resultado</li>
 * </ul>
 */
@Component
public class VerifyDeliverableWorker {

    private static final Logger log = LoggerFactory.getLogger(VerifyDeliverableWorker.class);

    private final DeliverableRepository deliverableRepo;

    public VerifyDeliverableWorker(DeliverableRepository deliverableRepo) {
        this.deliverableRepo = deliverableRepo;
    }

    @JobWorker(type = "verifyDeliverable")
    public Map<String, Object> verifyDeliverable(@Variable String orderId,
                                                  @Variable String deliverableId,
                                                  @Variable(required = false) String expectedKind) {
        log.info("Verificando deliverable: orderId={} deliverableId={}", orderId, deliverableId);

        Optional<Deliverable> found = deliverableRepo.findById(UUID.fromString(deliverableId));

        if (found.isEmpty()) {
            log.warn("Deliverable {} no encontrado para orden {}", deliverableId, orderId);
            return Map.of("deliverableVerified", false, "verificationReason", "Deliverable not found");
        }

        Deliverable d = found.get();

        if (!d.getOrderId().toString().equals(orderId)) {
            log.warn("Deliverable {} no pertenece a la orden {}", deliverableId, orderId);
            return Map.of("deliverableVerified", false, "verificationReason", "Order mismatch");
        }

        if (expectedKind != null && !expectedKind.equals(d.getKind().name())) {
            log.warn("Kind incorrecto: esperado={} actual={}", expectedKind, d.getKind());
            return Map.of("deliverableVerified", false,
                    "verificationReason", "Kind mismatch: expected " + expectedKind
                            + ", got " + d.getKind().name());
        }

        if (d.getSha256() == null || d.getSha256().isBlank()) {
            return Map.of("deliverableVerified", false, "verificationReason", "Missing SHA-256 checksum");
        }

        log.info("Deliverable {} verificado correctamente", deliverableId);
        return Map.of("deliverableVerified", true, "verificationReason", "OK");
    }
}
