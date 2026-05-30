package com.magenta.servicios.domain.port.out;

import com.magenta.servicios.domain.model.Partner;
import com.magenta.servicios.domain.model.ServiceCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartnerRepository {
    Partner save(Partner partner);
    Optional<Partner> findById(UUID id);
    List<Partner> findActiveByServiceAndZone(ServiceCode serviceCode, UUID zoneId);
    List<Partner> findAll(UUID tenantId);
}
