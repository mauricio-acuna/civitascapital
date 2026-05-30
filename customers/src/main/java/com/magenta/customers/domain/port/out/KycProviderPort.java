package com.magenta.customers.domain.port.out;

import java.util.UUID;

public interface KycProviderPort {
    /**
     * Inicia sesión KYC con el proveedor configurado y devuelve la URL de redirección
     * al iframe/SDK del proveedor.
     */
    String startSession(UUID customerId, String returnUrl);

    /**
     * Verifica la firma HMAC del callback del proveedor (replay-protection).
     * @param payload   body raw del callback
     * @param signature cabecera X-KYC-Signature
     * @param timestamp epoch del callback (anti-replay)
     */
    void verifyCallbackSignature(byte[] payload, String signature, long timestamp);
}
