package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ComputedAffordability {
    BigDecimal maxAffordablePaymentBdE;
    BigDecimal maxAffordablePaymentInternal;
    BigDecimal targetTicketPrice;
    BigDecimal savingsRunwayMonths;
}
