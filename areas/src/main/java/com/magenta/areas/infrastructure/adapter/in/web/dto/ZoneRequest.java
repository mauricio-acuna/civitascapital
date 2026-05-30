package com.magenta.areas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ZoneRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull String type,
        UUID parentId,
        @NotNull Double lat,
        @NotNull Double lng,
        String ineCode,
        Set<String> postalCodes,
        Integer population,
        BigDecimal areaKm2,
        Set<String> tags
) {}
