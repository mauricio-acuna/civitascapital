package com.magenta.areas.application;

import com.magenta.areas.domain.event.ZoneEnrichmentUpdated;
import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.ZoneEnrichment;
import com.magenta.areas.domain.port.in.UpdateEnrichmentPort;
import com.magenta.areas.domain.port.out.EnrichmentRepositoryPort;
import com.magenta.areas.domain.port.out.OutboxPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateEnrichmentUseCase implements UpdateEnrichmentPort {

    private final EnrichmentRepositoryPort enrichmentRepo;
    private final ZoneRepositoryPort zoneRepo;
    private final OutboxPort outbox;

    public UpdateEnrichmentUseCase(EnrichmentRepositoryPort enrichmentRepo,
                                    ZoneRepositoryPort zoneRepo, OutboxPort outbox) {
        this.enrichmentRepo = enrichmentRepo;
        this.zoneRepo       = zoneRepo;
        this.outbox         = outbox;
    }

    @Override
    public ZoneEnrichment execute(Command command) {
        var zone = zoneRepo.findById(command.zoneId())
                .orElseThrow(() -> new ZoneNotFoundException(command.zoneId()));

        ZoneEnrichment enrichment = enrichmentRepo.findByZoneId(command.zoneId())
                .orElse(ZoneEnrichment.empty(command.zoneId(), zone.getTenantId()));

        enrichment.update(command.fiberCoveragePct(), command.hasHospital(),
                command.hospitalKind(), command.trainToHubMinutes(),
                command.highwayDistanceKm(), command.supermarketsCount(),
                command.riskOccupationScore(), command.depopulationRisk(),
                command.qualityOfLifeIndex());

        enrichmentRepo.save(enrichment);

        outbox.publish(new ZoneEnrichmentUpdated(enrichment.getZoneId(), enrichment.getTenantId(),
                enrichment.getFiberCoveragePct(), enrichment.getHospitalKind(),
                enrichment.getDepopulationRisk(), command.actorId()));

        return enrichment;
    }
}
