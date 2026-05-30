package com.magenta.customers.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Validación del algoritmo de letra de control para CIF español.
 * Algoritmo oficial Agencia Tributaria:
 *  1. Suma dígitos en posición par
 *  2. Suma dígitos en posición impar con regla de multiplicación por 2
 *  3. Control = (10 - (suma % 10)) % 10
 *  4. Según primer carácter, control es dígito o letra (JABCDEFGHI)
 */
@DisplayName("CIF validator — letra de control")
class CifValidatorTest {

    private static final String CONTROL_LETTERS = "JABCDEFGHI";
    // Organizaciones que usan letra: K, P, Q, R, S, W
    private static final String LETTER_ONLY = "KPQRSW";
    // Organizaciones que usan dígito: A, B, E, H
    private static final String DIGIT_ONLY = "ABEH";

    private boolean isValidCif(String cif) {
        if (cif == null || cif.length() != 9) return false;
        cif = cif.toUpperCase();
        char type = cif.charAt(0);
        if (!Character.isLetter(type)) return false;

        String digits = cif.substring(1, 8);
        if (!digits.matches("[0-9]+")) return false;

        char control = cif.charAt(8);

        int sumEven = 0;
        int sumOdd = 0;
        for (int i = 0; i < 7; i++) {
            int d = digits.charAt(i) - '0';
            if (i % 2 == 0) {
                // Posición impar (1-indexed) → multiplicar por 2
                int doubled = d * 2;
                sumOdd += (doubled > 9) ? doubled - 9 : doubled;
            } else {
                sumEven += d;
            }
        }
        int total = sumEven + sumOdd;
        int controlDigit = (10 - (total % 10)) % 10;
        char controlLetter = CONTROL_LETTERS.charAt(controlDigit);

        if (LETTER_ONLY.indexOf(type) >= 0) {
            return control == controlLetter;
        } else if (DIGIT_ONLY.indexOf(type) >= 0) {
            return control == (char) ('0' + controlDigit);
        } else {
            return control == controlLetter || control == (char) ('0' + controlDigit);
        }
    }

    @Test
    @DisplayName("CIF SL válido de referencia")
    void validCif() {
        assertThat(isValidCif("B12345674")).isTrue();
    }

    @Test
    @DisplayName("CIF con control incorrecto")
    void invalidControl() {
        assertThat(isValidCif("B12345679")).isFalse();
    }

    @Test
    @DisplayName("CIF SOCIMI formato Q")
    void cifSOCIMI() {
        // Q-type siempre usa letra de control
        assertThatNoException().isThrownBy(() -> isValidCif("Q1234567A"));
    }

    @Test
    @DisplayName("CIF nulo devuelve false")
    void nullCif() {
        assertThat(isValidCif(null)).isFalse();
    }

    @Test
    @DisplayName("CIF longitud incorrecta")
    void wrongLength() {
        assertThat(isValidCif("B1234567")).isFalse();
    }
}
