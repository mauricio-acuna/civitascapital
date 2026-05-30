package com.magenta.banks.infrastructure.adapter.out.persistence.bank;

import com.magenta.banks.domain.model.Rating;
import com.magenta.banks.domain.model.bank.Bank;
import com.magenta.banks.domain.model.bank.ContactChannel;
import com.magenta.banks.domain.model.bank.ContactChannelType;
import com.magenta.banks.domain.port.out.BankRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class BankRepositoryAdapter implements BankRepository {

    private final BankJpaRepository jpaRepository;

    public BankRepositoryAdapter(BankJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Bank save(Bank bank) {
        BankJpaEntity entity = toEntity(bank);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Bank> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Bank> findAllActive(UUID tenantId) {
        return jpaRepository.findAllActiveByTenantId(tenantId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    // ── Mappers ─────────────────────────────────────────────────────────────

    private Bank toDomain(BankJpaEntity e) {
        List<ContactChannel> channels = e.getContactChannels().stream()
                .map(c -> new ContactChannel(c.getId(),
                        ContactChannelType.valueOf(c.getType()), c.getValue(), c.getLabel()))
                .collect(Collectors.toList());
        return new Bank(e.getId(), e.getTenantId(), e.getCode(), e.getName(), e.getBrand(),
                e.getCountry(), e.getBdeRegistryNumber(),
                e.getRating() != null ? Rating.valueOf(e.getRating().replace("+", "_PLUS").replace("-", "_MINUS")) : null,
                e.getLogoUrl(), e.getWebsiteUrl(), e.isActive(), channels,
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy(), e.getVersion());
    }

    private BankJpaEntity toEntity(Bank b) {
        BankJpaEntity e = new BankJpaEntity();
        e.setId(b.id());
        e.setTenantId(b.tenantId());
        e.setCode(b.code());
        e.setName(b.name());
        e.setBrand(b.brand());
        e.setCountry(b.country());
        e.setBdeRegistryNumber(b.bdeRegistryNumber());
        e.setRating(b.rating() != null ? b.rating().name() : null);
        e.setLogoUrl(b.logoUrl());
        e.setWebsiteUrl(b.websiteUrl());
        e.setActive(b.active());
        e.setCreatedAt(b.createdAt());
        e.setUpdatedAt(b.updatedAt());
        e.setCreatedBy(b.createdBy() != null ? b.createdBy() : "system");
        e.setUpdatedBy(b.updatedBy() != null ? b.updatedBy() : "system");
        return e;
    }
}
