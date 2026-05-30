package com.magenta.customers.domain.port.out;

import com.magenta.customers.domain.model.Customer;
import com.magenta.customers.domain.model.CustomerStatus;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByKeycloakUserId(String keycloakUserId);
    boolean existsByNifHash(String nifHash);
    boolean existsByEmailHash(String emailHash);
    void softDelete(UUID id, String deletedBy);
    Customer updateStatus(UUID id, CustomerStatus status, String updatedBy, long version);
}
