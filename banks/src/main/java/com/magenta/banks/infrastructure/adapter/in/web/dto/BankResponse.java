package com.magenta.banks.infrastructure.adapter.in.web.dto;

import com.magenta.banks.domain.model.bank.Bank;

import java.util.UUID;

public record BankResponse(
    UUID id,
    String code,
    String name,
    String brand,
    String country,
    String rating,
    String logoUrl,
    String websiteUrl,
    boolean active
) {
    public static BankResponse from(Bank b) {
        return new BankResponse(b.id(), b.code(), b.name(), b.brand(), b.country(),
                b.rating() != null ? b.rating().name() : null,
                b.logoUrl(), b.websiteUrl(), b.active());
    }
}
