package com.magenta.areas.application;

import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.model.ZoneType;
import com.magenta.areas.domain.port.in.SearchZonesPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchZonesUseCase implements SearchZonesPort {

    private final ZoneRepositoryPort repository;

    public SearchZonesUseCase(ZoneRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Zone> execute(Query query) {
        if (query.text() == null || query.text().isBlank()) {
            return List.of();
        }
        int limit = Math.min(query.limit(), 50); // cap de seguridad
        List<ZoneType> types = query.types() != null ? query.types() : List.of();
        return repository.searchByText(query.text().strip(), types, limit);
    }
}
