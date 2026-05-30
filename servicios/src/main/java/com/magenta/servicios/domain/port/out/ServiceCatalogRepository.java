package com.magenta.servicios.domain.port.out;

import com.magenta.servicios.domain.model.ServiceCode;
import com.magenta.servicios.domain.model.ServiceDefinition;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogRepository {
    List<ServiceDefinition> findAllActive();
    Optional<ServiceDefinition> findByCode(ServiceCode code);
    void save(ServiceDefinition definition);
}
