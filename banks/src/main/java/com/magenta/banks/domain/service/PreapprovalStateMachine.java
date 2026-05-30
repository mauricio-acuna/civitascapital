package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.PreapprovalStatus;

import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados para Preapproval.
 * Centraliza las transiciones permitidas.
 *
 * REQUESTED ──> IN_REVIEW ──> APPROVED ──> EXPIRED (auto)
 *    │             │
 *    │             └──> REJECTED
 *    └──> REJECTED (validación previa)
 */
public class PreapprovalStateMachine {

    private static final Map<PreapprovalStatus, Set<PreapprovalStatus>> TRANSITIONS = Map.of(
        PreapprovalStatus.REQUESTED,  Set.of(PreapprovalStatus.IN_REVIEW, PreapprovalStatus.REJECTED),
        PreapprovalStatus.IN_REVIEW,  Set.of(PreapprovalStatus.APPROVED,  PreapprovalStatus.REJECTED),
        PreapprovalStatus.APPROVED,   Set.of(PreapprovalStatus.EXPIRED),
        PreapprovalStatus.REJECTED,   Set.of(),
        PreapprovalStatus.EXPIRED,    Set.of()
    );

    public void assertTransitionAllowed(PreapprovalStatus from, PreapprovalStatus to) {
        Set<PreapprovalStatus> allowed = TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new IllegalStateException(
                "Transition from %s to %s is not allowed".formatted(from, to));
        }
    }

    public boolean isTerminal(PreapprovalStatus status) {
        return status == PreapprovalStatus.REJECTED || status == PreapprovalStatus.EXPIRED;
    }
}
