package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.PreapprovalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PreapprovalStateMachineTest {

    private PreapprovalStateMachine fsm;

    @BeforeEach
    void setUp() { fsm = new PreapprovalStateMachine(); }

    @Test @DisplayName("REQUESTED → IN_REVIEW permitido")
    void requestedToInReview() {
        assertThatNoException().isThrownBy(
            () -> fsm.assertTransitionAllowed(PreapprovalStatus.REQUESTED, PreapprovalStatus.IN_REVIEW));
    }

    @Test @DisplayName("REQUESTED → REJECTED permitido (validación previa)")
    void requestedToRejected() {
        assertThatNoException().isThrownBy(
            () -> fsm.assertTransitionAllowed(PreapprovalStatus.REQUESTED, PreapprovalStatus.REJECTED));
    }

    @Test @DisplayName("IN_REVIEW → APPROVED permitido")
    void inReviewToApproved() {
        assertThatNoException().isThrownBy(
            () -> fsm.assertTransitionAllowed(PreapprovalStatus.IN_REVIEW, PreapprovalStatus.APPROVED));
    }

    @Test @DisplayName("APPROVED → EXPIRED permitido (auto tras 90 días)")
    void approvedToExpired() {
        assertThatNoException().isThrownBy(
            () -> fsm.assertTransitionAllowed(PreapprovalStatus.APPROVED, PreapprovalStatus.EXPIRED));
    }

    @Test @DisplayName("REJECTED → cualquier estado: NO permitido")
    void rejectedIsTerminal() {
        assertThatThrownBy(() -> fsm.assertTransitionAllowed(PreapprovalStatus.REJECTED, PreapprovalStatus.APPROVED))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test @DisplayName("EXPIRED → cualquier estado: NO permitido")
    void expiredIsTerminal() {
        assertThatThrownBy(() -> fsm.assertTransitionAllowed(PreapprovalStatus.EXPIRED, PreapprovalStatus.IN_REVIEW))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test @DisplayName("REQUESTED → APPROVED directamente: NO permitido")
    void requestedToApprovedFails() {
        assertThatThrownBy(() -> fsm.assertTransitionAllowed(PreapprovalStatus.REQUESTED, PreapprovalStatus.APPROVED))
            .isInstanceOf(IllegalStateException.class);
    }
}
