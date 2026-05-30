package com.magenta.customers.infrastructure.adapter.out.crypto;

import com.magenta.customers.domain.port.out.PiiCryptoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTransitOperations;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Implementación de PII crypto con Vault Transit API (envelope encryption).
 * - encrypt/decrypt: Vault transit (AES-256-GCM managed by Vault)
 * - hmac: HMAC-SHA256 local con pepper del config
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VaultPiiCryptoAdapter implements PiiCryptoPort {

    private final VaultTransitOperations vaultTransit;

    @Value("${magenta.pii.vault-key-name}")
    private String keyName;

    @Value("${magenta.pii.hmac-pepper:${random.uuid}}")
    private String hmacPepper;

    @Override
    public byte[] encrypt(String plaintext) {
        if (plaintext == null) return null;
        // Vault transit devuelve string "vault:v1:..." en Base64
        String ciphertext = vaultTransit.encrypt(keyName, plaintext);
        return ciphertext.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String decrypt(byte[] ciphertext) {
        if (ciphertext == null) return null;
        return vaultTransit.decrypt(keyName, new String(ciphertext, StandardCharsets.UTF_8));
    }

    @Override
    public String hmac(String value) {
        if (value == null) return null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(
                    hmacPepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] hash = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }
}
