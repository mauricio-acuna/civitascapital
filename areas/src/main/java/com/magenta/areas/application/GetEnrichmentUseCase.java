package com.magenta.areas.application;

import com.magenta.areas.domain.model.ZoneEnrichment;
import com.magenta.areas.domain.port.in.GetEnrichmentPort;
import com.magenta.areas.domain.port.out.EnrichmentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetEnrichmentUseCase implements GetEnrichmentPort {

    private final EnrichmentRepositoryPort repository;

    public GetEnrichmentUseCase(EnrichmentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ZoneEnrichment> execute(UUID zoneId) {
        return repository.findByZoneId(zoneId);
    }
}
