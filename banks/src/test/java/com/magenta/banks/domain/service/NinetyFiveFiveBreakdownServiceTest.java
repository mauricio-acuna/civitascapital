package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.loansimulation.TaxInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NinetyFiveFiveBreakdownServiceTest {

    private final NinetyFiveFiveBreakdownService service = new NinetyFiveFiveBreakdownService();

    @Test
    @DisplayName("Desglosa 90+5+5 de obra nueva con IVA y AJD")
    void newBuildBreakdown() {
        var result = service.calculate(
                BigDecimal.valueOf(300_000),
                new TaxInfo(BigDecimal.valueOf(10), BigDecimal.valueOf(1.5), null),
                true);

        assertThat(result.bankLoan()).isEqualByComparingTo("270000.00");
        assertThat(result.developerDeferred()).isEqualByComparingTo("15000.00");
        assertThat(result.buyerDownPayment()).isEqualByComparingTo("15000.00");
        assertThat(result.taxes()).isEqualByComparingTo("34500.00");
        assertThat(result.closingCosts()).isEqualByComparingTo("3600.00");
        assertThat(result.requiredOwnFundsAtSigning()).isEqualByComparingTo("53100.00");
        assertThat(result.totalCommittedOwnFunds()).isEqualByComparingTo("68100.00");
    }

    @Test
    @DisplayName("Desglosa segunda mano con ITP")
    void secondHandBreakdown() {
        var result = service.calculate(
                BigDecimal.valueOf(240_000),
                new TaxInfo(null, null, BigDecimal.valueOf(7)),
                false);

        assertThat(result.bankLoan()).isEqualByComparingTo("216000.00");
        assertThat(result.taxes()).isEqualByComparingTo("16800.00");
        assertThat(result.requiredOwnFundsAtSigning()).isEqualByComparingTo("31680.00");
        assertThat(result.totalCommittedOwnFunds()).isEqualByComparingTo("43680.00");
    }

    @Test
    @DisplayName("Rechaza precios nulos o no positivos")
    void rejectsInvalidPrice() {
        assertThatThrownBy(() -> service.calculate(BigDecimal.ZERO, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("propertyPrice");
    }
}

