package com.magenta.areas.application;

import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.port.in.ResolvePointPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ResolvePointUseCase implements ResolvePointPort {

    private final ZoneRepositoryPort repository;

    public ResolvePointUseCase(ZoneRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Zone> execute(double lat, double lng) {
        return repository.resolvePoint(lat, lng);
    }
}
