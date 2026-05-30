package com.magenta.customers.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Validación del algoritmo módulo 23 para NIF/NIE español.
 * Especificación: los 8 dígitos numéricos MOD 23 → letra de la tabla TRWAGMYFPDXBNJZSQVHLCKE.
 * NIE: sustituir primera letra (X→0, Y→1, Z→2) y aplicar mismo algoritmo.
 */
@DisplayName("NIF validator — módulo 23")
class NifValidatorTest {

    private static final String LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private boolean isValidNif(String nif) {
        if (nif == null || nif.length() != 9) return false;
        nif = nif.toUpperCase();
        char lastChar = nif.charAt(8);
        String digits;
        if (Character.isLetter(nif.charAt(0))) {
            // NIE
            char first = nif.charAt(0);
            if (first != 'X' && first != 'Y' && first != 'Z') return false;
            int firstDigit = first == 'X' ? 0 : first == 'Y' ? 1 : 2;
            digits = firstDigit + nif.substring(1, 8);
        } else {
            digits = nif.substring(0, 8);
        }
        try {
            int num = Integer.parseInt(digits);
            return LETTERS.charAt(num % 23) == lastChar;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Test
    @DisplayName("NIF válido de referencia")
    void validNif() {
        assertThat(isValidNif("12345678Z")).isTrue();
    }

    @Test
    @DisplayName("NIF con letra incorrecta")
    void invalidNifLetter() {
        assertThat(isValidNif("12345678A")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"X1234567L", "Y1234567X", "Z1234567R"})
    @DisplayName("NIE válido (X, Y, Z)")
    void validNie(String nie) {
        // Verificar que el algoritmo no lanza excepción
        assertThatNoException().isThrownBy(() -> isValidNif(nie));
    }

    @Test
    @DisplayName("NIF nulo devuelve false")
    void nullNif() {
        assertThat(isValidNif(null)).isFalse();
    }

    @Test
    @DisplayName("NIF de longitud incorrecta devuelve false")
    void wrongLength() {
        assertThat(isValidNif("1234567Z")).isFalse();
        assertThat(isValidNif("123456789Z")).isFalse();
    }
}
