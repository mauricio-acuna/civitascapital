package com.magenta.customers.domain.port.out;

/**
 * Puerto para cifrado/descifrado de campos PII.
 * La implementación usa Vault Transit API (envelope encryption).
 */
public interface PiiCryptoPort {
    /** Cifra el valor en claro y devuelve el cipher-text Base64. */
    byte[] encrypt(String plaintext);

    /** Descifra el cipher-text y devuelve el valor en claro. */
    String decrypt(byte[] ciphertext);

    /** HMAC-SHA256 + pepper para hashing determinista (búsqueda por NIF/email). */
    String hmac(String value);
}
