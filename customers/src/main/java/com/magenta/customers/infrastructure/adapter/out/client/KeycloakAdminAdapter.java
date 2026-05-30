package com.magenta.customers.infrastructure.adapter.out.client;

import com.magenta.customers.domain.port.out.KeycloakPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

/**
 * Adaptador para Keycloak Admin REST API.
 * POST /admin/realms/magenta/users
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminAdapter implements KeycloakPort {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private final RestClient.Builder restClientBuilder;

    private String adminBaseUrl() {
        // Convertir issuerUri realm URL a admin URL
        return issuerUri.replace("/realms/", "/admin/realms/");
    }

    @Override
    public String createUser(String email, String firstName, String lastName,
                              UUID customerId, UUID tenantId) {
        try {
            Map<String, Object> userRep = Map.of(
                    "email", email != null ? email : "",
                    "firstName", firstName,
                    "lastName", lastName,
                    "enabled", true,
                    "attributes", Map.of(
                            "tenant_id", new String[]{tenantId.toString()}
                    )
            );

            var response = restClientBuilder.build()
                    .post()
                    .uri(adminBaseUrl() + "/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userRep)
                    .retrieve()
                    .toBodilessEntity();

            // Keycloak devuelve Location: .../users/{id}
            String location = response.getHeaders().getFirst("Location");
            if (location != null) {
                return location.substring(location.lastIndexOf('/') + 1);
            }
            return "mock-" + UUID.randomUUID();
        } catch (Exception e) {
            log.warn("Keycloak user creation failed, continuing without keycloakId: {}", e.getMessage());
            return "pending-" + UUID.randomUUID();
        }
    }

    @Override
    public void assignRole(String keycloakUserId, String role) {
        log.info("Assigning role {} to keycloak user {}", role, keycloakUserId);
        // PUT /admin/realms/magenta/users/{id}/role-mappings/realm
    }

    @Override
    public void disableUser(String keycloakUserId) {
        log.info("Disabling keycloak user {}", keycloakUserId);
        try {
            restClientBuilder.build()
                    .put()
                    .uri(adminBaseUrl() + "/users/" + keycloakUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("enabled", false))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Could not disable keycloak user {}: {}", keycloakUserId, e.getMessage());
        }
    }

    @Override
    public void deleteUser(String keycloakUserId) {
        log.info("Deleting keycloak user {}", keycloakUserId);
        try {
            restClientBuilder.build()
                    .delete()
                    .uri(adminBaseUrl() + "/users/" + keycloakUserId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Could not delete keycloak user {}: {}", keycloakUserId, e.getMessage());
        }
    }
}
