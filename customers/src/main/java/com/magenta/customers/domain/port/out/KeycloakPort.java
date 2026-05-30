package com.magenta.customers.domain.port.out;

import java.util.UUID;

public interface KeycloakPort {
    /**
     * Crea usuario en el realm magenta y asigna el rol ROLE_CUSTOMER.
     * @return keycloakUserId
     */
    String createUser(String email, String firstName, String lastName, UUID customerId, UUID tenantId);

    /** Asigna un rol adicional al usuario. */
    void assignRole(String keycloakUserId, String role);

    /** Deshabilita el usuario (soft-delete RGPD). */
    void disableUser(String keycloakUserId);

    /** Elimina el usuario del realm (hard-delete RGPD). */
    void deleteUser(String keycloakUserId);
}
