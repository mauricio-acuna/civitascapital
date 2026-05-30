package com.magenta.customers.infrastructure.adapter.out.client;

import com.magenta.customers.domain.port.out.KycProviderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador para el proveedor KYC (IDNow por defecto).
 * Implementa verificación HMAC-SHA256 del callback con protección anti-replay.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KycProviderAdapter implements KycProviderPort {

    @Value("${magenta.kyc.callback-secret}")
    private String callbackSecret;

    @Value("${magenta.kyc.callback-replay-window-seconds:300}")
    private long replayWindowSeconds;

    private final RestClient.Builder restClientBuilder;

    @Override
    public String startSession(UUID customerId, String returnUrl) {
        // En producción: POST al SDK de IDNow/Onfido
        // Aquí mock para desarrollo
        log.info("Starting KYC session for customer={}", customerId);
        return "https://idnow.io/sessions/mock-" + customerId + "?returnUrl=" + returnUrl;
    }

    @Override
    public void verifyCallbackSignature(byte[] payload, String signature, long timestamp) {
        // 1. Anti-replay: el timestamp no puede ser más antiguo que la ventana
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > replayWindowSeconds) {
            throw new SecurityException("KYC callback timestamp out of replay window");
        }

        // 2. Verificar HMAC-SHA256
        String expected = computeHmac(payload, timestamp);
        if (!constantTimeEquals(expected, signature)) {
            throw new SecurityException("KYC callback signature mismatch");
        }
    }

    private String computeHmac(byte[] payload, long timestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(callbackSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(String.valueOf(timestamp).getBytes(StandardCharsets.UTF_8));
            mac.update(".".getBytes(StandardCharsets.UTF_8));
            byte[] hash = mac.doFinal(payload);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }

    /** Comparación en tiempo constante para prevenir timing attacks. */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        if (aBytes.length != bBytes.length) return false;
        int diff = 0;
        for (int i = 0; i < aBytes.length; i++) {
            diff |= aBytes[i] ^ bBytes[i];
        }
        return diff == 0;
    }
}
