package com.magenta.servicios.domain.port.out;

import java.util.UUID;

public interface CustomerClientPort {
    CustomerProfile getProfile(UUID customerId);
    boolean hasKycApproved(UUID customerId);

    record CustomerProfile(UUID id, String fullName, String email,
                           java.math.BigDecimal monthlyIncome, boolean kycApproved) {}
}
