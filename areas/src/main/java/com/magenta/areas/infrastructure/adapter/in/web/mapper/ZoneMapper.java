package com.magenta.areas.infrastructure.adapter.in.web.mapper;

import com.magenta.areas.domain.model.*;
import com.magenta.areas.infrastructure.adapter.in.web.dto.ZoneDetailResponse;
import com.magenta.areas.infrastructure.adapter.in.web.dto.ZoneRequest;
import org.mapstruct.*;

import java.util.UUID;

/**
 * Mapper MapStruct entre el dominio {@link Zone} y los DTOs REST.
 *
 * Convenciones:
 * - Domain → DTO: mapping explícito de campos con nombres distintos.
 * - DTO → Domain: se delega a {@code Zone.create()} para respetar invariantes del agregado.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ZoneMapper {

    /**
     * Mapea el aggregate {@link Zone} al DTO de detalle completo.
     * Los campos {@code enrichment} y {@code latestPrice} se rellenan
     * en el use case, no aquí.
     */
    @Mapping(target = "type",     expression = "java(zone.getType().name())")
    @Mapping(target = "status",   expression = "java(zone.getStatus().name())")
    @Mapping(target = "parent",   ignore = true)   // rellena ZoneController con llamada separada
    @Mapping(target = "enrichment", ignore = true)
    @Mapping(target = "latestPrice", ignore = true)
    @Mapping(target = "centroid", source = "zone.centroid")
    ZoneDetailResponse toDetailResponse(Zone zone);

    /**
     * Mapea {@link GeoPoint} → {@link ZoneDetailResponse.GeoPointDto}.
     */
    default ZoneDetailResponse.GeoPointDto toGeoPointDto(GeoPoint geoPoint) {
        if (geoPoint == null) return null;
        return new ZoneDetailResponse.GeoPointDto(geoPoint.lat(), geoPoint.lng());
    }

    /**
     * Crea un {@link Zone} desde un {@link ZoneRequest} de creación.
     * El ID y tenantId se asignan en el use case (no en el mapper).
     */
    default Zone toDomain(ZoneRequest request, UUID id, UUID tenantId) {
        return Zone.create(
                id,
                tenantId,
                request.code(),
                request.name(),
                ZoneType.valueOf(request.type()),
                request.parentId(),
                new GeoPoint(request.lat(), request.lng())
        );
    }
}
