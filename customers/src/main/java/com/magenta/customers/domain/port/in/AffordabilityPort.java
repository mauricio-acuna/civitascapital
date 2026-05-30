package com.magenta.customers.domain.port.in;

import com.magenta.customers.domain.model.ComputedAffordability;
import com.magenta.customers.domain.model.FinancialSnapshot;

public interface AffordabilityPort {
    /** Calcula capacidad hipotecaria a partir del snapshot financiero. */
    ComputedAffordability compute(FinancialSnapshot snapshot);
}
