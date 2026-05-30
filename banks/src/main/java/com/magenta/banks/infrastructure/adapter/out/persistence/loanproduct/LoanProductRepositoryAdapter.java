package com.magenta.banks.infrastructure.adapter.out.persistence.loanproduct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.banks.domain.model.LoanCategory;
import com.magenta.banks.domain.model.ProductStatus;
import com.magenta.banks.domain.model.RateType;
import com.magenta.banks.domain.model.Scheme;
import com.magenta.banks.domain.model.loanproduct.EligibilityRules;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.model.loanproduct.RateInfo;
import com.magenta.banks.domain.model.pagination.PageResult;
import com.magenta.banks.domain.model.pagination.PageSpec;
import com.magenta.banks.domain.port.out.LoanProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class LoanProductRepositoryAdapter implements LoanProductRepository {

    private final LoanProductJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public LoanProductRepositoryAdapter(LoanProductJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper  = objectMapper;
    }

    @Override
    public LoanProduct save(LoanProduct product) {
        return toDomain(jpaRepository.save(toEntity(product)));
    }

    @Override
    public Optional<LoanProduct> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<LoanProduct> findActiveByBankId(UUID bankId) {
        return jpaRepository.findByBankIdAndStatus(bankId, "ACTIVE")
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<LoanProduct> search(UUID tenantId, Scheme scheme, BigDecimal ltvMin, Integer maxAge,
                                          BigDecimal ticketAmount, LoanCategory category, PageSpec page) {
        Pageable pageable = PageRequest.of(page.page(), page.size(), Sort.by("validFrom").descending());
        Page<LoanProduct> result = jpaRepository.search(tenantId, scheme, scheme != null ? scheme.name() : null,
                ltvMin, ticketAmount, category, category != null ? category.name() : null, pageable)
                .map(this::toDomain);
        return new PageResult<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public List<LoanProduct> findActiveByIds(List<UUID> ids) {
        return jpaRepository.findByIdIn(ids).stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ── Mappers ─────────────────────────────────────────────────────────────

    private LoanProduct toDomain(LoanProductJpaEntity e) {
        RateInfo rateInfo = new RateInfo(
                RateType.valueOf(e.getRateType()), e.getTinInitialPct(),
                e.getTinIndexReference(), e.getTinMarginPct(), e.getTinFixedYears());

        EligibilityRules eligibility = deserializeEligibility(e.getEligibility());
        List<String> bundling        = deserializeBundling(e.getBundling());

        return new LoanProduct(
                e.getId(), e.getTenantId(), e.getBankId(), e.getSku(), e.getName(),
                LoanCategory.valueOf(e.getCategory()), rateInfo,
                e.getLtvMaxPct(), e.getLtcMaxPct(), e.getTicketMin(), e.getTicketMax(),
                e.getTermMinMonths(), e.getTermMaxMonths(), eligibility, bundling,
                e.getFeeOpeningPct(), e.getFeeStudyPct(), e.getFeeEarlyRepaymentPct(),
                Scheme.valueOf(e.getScheme()), e.getPromoCode(),
                e.getValidFrom(), e.getValidTo(),
                ProductStatus.valueOf(e.getStatus()),
                e.getCreatedAt(), e.getUpdatedAt(), e.getVersion());
    }

    private LoanProductJpaEntity toEntity(LoanProduct p) {
        LoanProductJpaEntity e = new LoanProductJpaEntity();
        e.setId(p.id());
        e.setTenantId(p.tenantId());
        e.setBankId(p.bankId());
        e.setSku(p.sku());
        e.setName(p.name());
        e.setCategory(p.category().name());
        e.setRateType(p.rateInfo().rateType().name());
        e.setTinInitialPct(p.rateInfo().initialPct());
        e.setTinIndexReference(p.rateInfo().indexReference());
        e.setTinMarginPct(p.rateInfo().marginPct());
        e.setTinFixedYears(p.rateInfo().fixedYears());
        e.setLtvMaxPct(p.ltvMaxPct());
        e.setLtcMaxPct(p.ltcMaxPct());
        e.setTicketMin(p.ticketMin());
        e.setTicketMax(p.ticketMax());
        e.setTermMinMonths(p.termMinMonths());
        e.setTermMaxMonths(p.termMaxMonths());
        e.setEligibility(serializeEligibility(p.eligibility()));
        e.setBundling(serializeBundling(p.bundling()));
        e.setFeeOpeningPct(p.feeOpeningPct());
        e.setFeeStudyPct(p.feeStudyPct());
        e.setFeeEarlyRepaymentPct(p.feeEarlyRepaymentPct());
        e.setScheme(p.scheme().name());
        e.setPromoCode(p.promoCode());
        e.setValidFrom(p.validFrom());
        e.setValidTo(p.validTo());
        e.setStatus(p.status().name());
        e.setCreatedAt(p.createdAt() != null ? p.createdAt() : Instant.now());
        e.setUpdatedAt(p.updatedAt() != null ? p.updatedAt() : Instant.now());
        return e;
    }

    private EligibilityRules deserializeEligibility(String json) {
        try {
            return objectMapper.readValue(json, EligibilityRules.class);
        } catch (Exception ex) {
            return EligibilityRules.empty();
        }
    }

    private List<String> deserializeBundling(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String serializeEligibility(EligibilityRules rules) {
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (JsonProcessingException ex) {
            return "{\"all\":[]}";
        }
    }

    private String serializeBundling(List<String> bundling) {
        try {
            return objectMapper.writeValueAsString(bundling);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }
}
