package com.magenta.areas.application;

import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.port.in.GetZonePort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetZoneUseCase implements GetZonePort {

    private final ZoneRepositoryPort repository;

    public GetZoneUseCase(ZoneRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable(value = "zones", key = "#id")
    public Optional<Zone> byId(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Zone> children(UUID parentId) {
        return repository.findChildren(parentId);
    }

    @Override
    @Cacheable(value = "zoneAncestors", key = "#id")
    public List<Zone> ancestors(UUID id) {
        if (repository.findById(id).isEmpty()) {
            throw new ZoneNotFoundException(id);
        }
        return repository.findAncestors(id);
    }

    @Override
    public List<Zone> byPostalCode(String postalCode) {
        return repository.findByPostalCode(postalCode);
    }
}
