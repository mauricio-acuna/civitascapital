package com.magenta.servicios.domain;

import com.magenta.servicios.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ServiceOrderStateMachineTest {

    @Test
    void shouldTransitionFromDraftToQuoted() {
        ServiceOrder order = buildOrder();
        order.quote(new BigDecimal("299.00"), Instant.now().plus(72, ChronoUnit.HOURS), "SYSTEM");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.QUOTED);
        assertThat(order.getPriceQuoted()).isEqualByComparingTo("299.00");
    }

    @Test
    void shouldTransitionFromQuotedToAccepted() {
        ServiceOrder order = buildOrder();
        order.quote(new BigDecimal("299.00"), Instant.now().plus(72, ChronoUnit.HOURS), "SYSTEM");
        order.accept("customer-uuid");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void shouldTransitionToInProgress() {
        ServiceOrder order = buildOrder();
        order.quote(new BigDecimal("299.00"), Instant.now().plus(72, ChronoUnit.HOURS), "SYSTEM");
        order.accept("customer-uuid");
        order.startWorkflow("pi_zeebe_12345", "SYSTEM");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(order.getWorkflowInstanceId()).isEqualTo("pi_zeebe_12345");
    }

    @Test
    void shouldFailWhenAcceptingFromDraft() {
        ServiceOrder order = buildOrder();

        assertThatThrownBy(() -> order.accept("actor"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("QUOTED");
    }

    @Test
    void shouldCancelOrder() {
        ServiceOrder order = buildOrder();
        order.quote(new BigDecimal("299.00"), Instant.now().plus(72, ChronoUnit.HOURS), "SYSTEM");
        order.cancel("Cancelación cliente", "customer");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldNotCancelCompletedOrder() {
        ServiceOrder order = buildOrder();
        order.quote(new BigDecimal("299.00"), Instant.now().plus(72, ChronoUnit.HOURS), "SYSTEM");
        order.accept("customer");
        order.startWorkflow("wf-123", "SYSTEM");
        order.complete(new BigDecimal("299.00"), "SYSTEM");

        assertThatThrownBy(() -> order.cancel("intento", "actor"))
                .isInstanceOf(IllegalStateException.class);
    }

    private ServiceOrder buildOrder() {
        return new ServiceOrder(
                UUID.randomUUID(), UUID.randomUUID(),
                ServiceCode.APPRAISAL, UUID.randomUUID(),
                UUID.randomUUID(), null, null,
                "{}", BigDecimal.ZERO, "EUR");
    }
}
