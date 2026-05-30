package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.model.PreapprovalStatus;
import com.magenta.banks.domain.model.preapproval.Preapproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PreapprovalRepository {
    Preapproval save(Preapproval preapproval);
    Optional<Preapproval> findById(UUID id);
    Page<Preapproval> findByCustomerId(UUID customerId, Pageable pageable);
    List<Preapproval> findExpired();  // APPROVED con expiresAt < now()
}
