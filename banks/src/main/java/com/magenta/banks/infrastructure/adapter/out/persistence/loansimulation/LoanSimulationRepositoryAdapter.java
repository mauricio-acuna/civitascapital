package com.magenta.banks.infrastructure.adapter.out.persistence.loansimulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.banks.domain.model.Verdict;
import com.magenta.banks.domain.model.loansimulation.*;
import com.magenta.banks.domain.port.out.LoanSimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LoanSimulationRepositoryAdapter implements LoanSimulationRepository {

    private final LoanSimulationJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public LoanSimulation save(LoanSimulation simulation) {
        LoanSimulationJpaEntity entity = toEntity(simulation);
        LoanSimulationJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LoanSimulation> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<LoanSimulation> findByCustomerId(UUID customerId) {
        return jpaRepository.findAllByCustomerId(customerId).stream()
                .map(this::toDomain)
                .toList();
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private LoanSimulationJpaEntity toEntity(LoanSimulation s) {
        LoanSimulationJpaEntity e = new LoanSimulationJpaEntity();
        e.setId(s.id());
        e.setTenantId(s.tenantId());
        e.setCustomerId(s.customerId());
        e.setProductId(s.productId());
        e.setBankId(s.tenantId()); // bankId stored same as tenantId for simplicity; adjust if separate field needed
        e.setPropertyId(s.propertyId());
        e.setPrice(s.propertyPrice());
        e.setLoanAmount(s.requestedAmount());
        e.setTermMonths(s.termMonths());
        e.setCreatedAt(s.createdAt());

        SimulationResult r = s.result();
        if (r != null) {
            e.setTinAppliedPct(r.tinApplied());
            e.setMonthlyPayment(r.monthlyPayment());
            e.setTaePct(r.tae());
            e.setTotalCost(r.totalCost());
            e.setTotalInterest(r.totalInterest());
            e.setEffortRatio(r.effortRatio());
            e.setDebtRatio(r.debtRatio());
            e.setLtvComputed(r.ltvComputed());
            e.setApprovabilityScore(r.approvabilityScore());
            e.setVerdict(r.verdict() != null ? r.verdict().name() : null);
            e.setRequiredOwnFunds(r.requiredOwnFunds());
            e.setFundsGap(r.fundsGap());
            e.setTaxes(writeJson(r.taxes()));
            e.setWarnings(writeJson(r.warnings()));
            e.setAlternatives(writeJson(r.alternatives()));
        }
        return e;
    }

    private LoanSimulation toDomain(LoanSimulationJpaEntity e) {
        TaxInfo taxes = readJson(e.getTaxes(), TaxInfo.class);
        List<String> warnings = readJsonList(e.getWarnings(), new TypeReference<>() {});
        List<SimulationResult.AlternativeProduct> alternatives = readJsonList(e.getAlternatives(), new TypeReference<>() {});

        SimulationResult result = new SimulationResult(
                e.getMonthlyPayment(),
                e.getTaePct(),
                e.getTinAppliedPct(),
                e.getTotalCost(),
                e.getTotalInterest(),
                e.getEffortRatio(),
                e.getDebtRatio(),
                e.getLtvComputed(),
                taxes,
                e.getRequiredOwnFunds(),
                e.getFundsGap(),
                e.getApprovabilityScore() != null ? e.getApprovabilityScore() : 0,
                e.getVerdict() != null ? Verdict.valueOf(e.getVerdict()) : null,
                warnings,
                alternatives
        );

        return new LoanSimulation(
                e.getId(),
                e.getTenantId(),
                e.getCustomerId(),
                e.getProductId(),
                e.getPropertyId(),
                null, // zoneId not persisted
                e.getLoanAmount(),
                e.getPrice(),
                null, // surfaceSqm not persisted
                null, // propertyType not persisted
                null, // operationType not persisted
                e.getTermMonths(),
                null, // borrower not persisted (derived from use case context)
                taxes,
                result,
                e.getCreatedAt()
        );
    }

    private String writeJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("JSON serialization error", ex);
        }
    }

    private <T> T readJson(String json, Class<T> type) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("JSON deserialization error", ex);
        }
    }

    private <T> T readJsonList(String json, TypeReference<T> typeRef) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("JSON deserialization error", ex);
        }
    }
}
