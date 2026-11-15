package ch.inss.openapi.joaswizard.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumbersTest {

    @Test
    void validNumbers() {
        for (String s : new String[]{"-1", "3.14", "-0.5", "42", " 7 ", "0"}) {
            assertTrue(Numbers.isNumber(s), s);
        }
    }

    @Test
    void invalidNumbers() {
        for (String s : new String[]{null, "", "  ", "1.2.3", "...", ".", "-", "1e5", "abc", "1.", ".5", "--1"}) {
            assertFalse(Numbers.isNumber(s), String.valueOf(s));
        }
    }
}
