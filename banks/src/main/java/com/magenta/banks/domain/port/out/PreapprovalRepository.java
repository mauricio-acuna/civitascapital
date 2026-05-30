package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.model.PreapprovalStatus;
import com.magenta.banks.domain.model.pagination.PageResult;
import com.magenta.banks.domain.model.pagination.PageSpec;
import com.magenta.banks.domain.model.preapproval.Preapproval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PreapprovalRepository {
    Preapproval save(Preapproval preapproval);
    Optional<Preapproval> findById(UUID id);
    PageResult<Preapproval> findByCustomerId(UUID customerId, PageSpec page);
    List<Preapproval> findExpired();  // APPROVED con expiresAt < now()
}
