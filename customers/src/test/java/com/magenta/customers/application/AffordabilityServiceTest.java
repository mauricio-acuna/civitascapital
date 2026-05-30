package com.magenta.customers.application;

import com.magenta.customers.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AffordabilityService — cálculos de capacidad hipotecaria")
class AffordabilityServiceTest {

    private AffordabilityService service;

    @BeforeEach
    void setUp() {
        service = new AffordabilityService();
        ReflectionTestUtils.setField(service, "bdeRatio", 0.40);
        ReflectionTestUtils.setField(service, "internalRatio", 0.30);
        ReflectionTestUtils.setField(service, "defaultTermMonths", 300);
        ReflectionTestUtils.setField(service, "defaultTin", 0.035);
        ReflectionTestUtils.setField(service, "defaultLtv", 0.80);
    }

    private FinancialSnapshot snapshot(double netIncome, double otherDebt, double ownFunds) {
        return FinancialSnapshot.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .asOf(LocalDate.now())
                .netIncomeMonthly(BigDecimal.valueOf(netIncome))
                .payments(12)
                .otherDebtMonthly(BigDecimal.valueOf(otherDebt))
                .ownFunds(BigDecimal.valueOf(ownFunds))
                .cirbeFlag(false)
                .existingProperties(0)
                .rentalIncomeMonthly(BigDecimal.ZERO)
                .confidence(BigDecimal.valueOf(0.5))
                .build();
    }

    @Test
    @DisplayName("maxPaymentBdE = netIncome × 0.40 − otherDebt")
    void maxPaymentBdE() {
        ComputedAffordability result = service.compute(snapshot(2000, 200, 10000));
        // 2000 × 0.40 − 200 = 600
        assertThat(result.getMaxAffordablePaymentBdE()).isEqualByComparingTo("600.00");
    }

    @Test
    @DisplayName("maxPaymentInternal = netIncome × 0.30 − otherDebt")
    void maxPaymentInternal() {
        ComputedAffordability result = service.compute(snapshot(2000, 200, 10000));
        // 2000 × 0.30 − 200 = 400
        assertThat(result.getMaxAffordablePaymentInternal()).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("Cuando otherDebt > netIncome × ratio → payment = 0")
    void paymentZeroWhenOverindebted() {
        ComputedAffordability result = service.compute(snapshot(1000, 500, 0));
        // 1000 × 0.30 − 500 = −200 → max(0, ...) = 0
        assertThat(result.getMaxAffordablePaymentInternal()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("targetTicketPrice > 0 cuando hay capacidad")
    void targetTicketPositive() {
        ComputedAffordability result = service.compute(snapshot(3000, 0, 20000));
        assertThat(result.getTargetTicketPrice()).isPositive();
    }

    @Test
    @DisplayName("savingsRunwayMonths = ownFunds / maxPaymentInternal")
    void savingsRunway() {
        ComputedAffordability result = service.compute(snapshot(2000, 200, 12000));
        // maxInternal = 400; runway = 12000 / 400 = 30
        assertThat(result.getSavingsRunwayMonths()).isEqualByComparingTo("30.0");
    }

    @Test
    @DisplayName("Sin fondos propios → savingsRunway = 0")
    void zeroSavingsRunway() {
        ComputedAffordability result = service.compute(snapshot(2000, 0, 0));
        assertThat(result.getSavingsRunwayMonths()).isEqualByComparingTo("0.0");
    }
}
