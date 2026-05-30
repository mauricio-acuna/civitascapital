package com.magenta.products.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record FinancingHint(
        Set<UUID> feasibleBankProductIds,
        UUID best90_5_5ProductId,
        Instant lastEvaluatedAt) {

    public boolean has90_5_5() {
        return best90_5_5ProductId != null;
    }

    public static FinancingHint empty() {
        return new FinancingHint(Set.of(), null, null);
    }
}
