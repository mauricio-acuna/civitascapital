package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.model.ServiceDefinition;
import com.magenta.servicios.domain.port.out.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListCatalogUseCase {

    private final ServiceCatalogRepository catalog;

    public ListCatalogUseCase(ServiceCatalogRepository catalog) {
        this.catalog = catalog;
    }

    public List<ServiceDefinition> execute() {
        return catalog.findAllActive();
    }
}
