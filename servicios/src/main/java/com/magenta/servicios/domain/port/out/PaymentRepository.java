package com.magenta.servicios.domain.port.out;

import com.magenta.servicios.domain.model.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID id);
    List<Payment> findByOrderId(UUID orderId);
}
