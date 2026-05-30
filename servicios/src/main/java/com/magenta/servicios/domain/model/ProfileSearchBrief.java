package com.magenta.servicios.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ProfileSearchBrief {

    private final UUID orderId;
    private BigDecimal minIncomeMultiple;
    private String contractType;
    private Short maxDependents;
    private Boolean requiresGuarantor;
    private Boolean petsAllowed;
    private LocalDate targetMoveInDate;
    private List<UUID> shortlist;

    public ProfileSearchBrief(UUID orderId, BigDecimal minIncomeMultiple, String contractType,
                               Short maxDependents, Boolean requiresGuarantor, Boolean petsAllowed,
                               LocalDate targetMoveInDate, List<UUID> shortlist) {
        this.orderId = orderId;
        this.minIncomeMultiple = minIncomeMultiple;
        this.contractType = contractType;
        this.maxDependents = maxDependents;
        this.requiresGuarantor = requiresGuarantor;
        this.petsAllowed = petsAllowed;
        this.targetMoveInDate = targetMoveInDate;
        this.shortlist = shortlist != null ? List.copyOf(shortlist) : List.of();
    }

    public void addCandidate(UUID candidateId) {
        List<UUID> updated = new java.util.ArrayList<>(shortlist);
        updated.add(candidateId);
        this.shortlist = List.copyOf(updated);
    }

    public UUID getOrderId() { return orderId; }
    public BigDecimal getMinIncomeMultiple() { return minIncomeMultiple; }
    public String getContractType() { return contractType; }
    public Short getMaxDependents() { return maxDependents; }
    public Boolean getRequiresGuarantor() { return requiresGuarantor; }
    public Boolean getPetsAllowed() { return petsAllowed; }
    public LocalDate getTargetMoveInDate() { return targetMoveInDate; }
    public List<UUID> getShortlist() { return shortlist; }
}
