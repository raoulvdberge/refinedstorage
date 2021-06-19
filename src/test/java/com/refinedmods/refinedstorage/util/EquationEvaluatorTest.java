package com.refinedmods.refinedstorage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EquationEvaluatorTest {

    private void assertFuzzyEquals(double expected, double actual) {
        assertEquals(expected, actual, 0.001D);
    }

    @Test
    void testConstants() {
        assertFuzzyEquals(1, EquationEvaluator.evaluate("1"));
        assertFuzzyEquals(122, EquationEvaluator.evaluate("122"));
        assertFuzzyEquals(4.2, EquationEvaluator.evaluate("4.20"));
    }

    @Test
    void testNegativeConstants() {
        assertFuzzyEquals(-1, EquationEvaluator.evaluate("-1"));
        assertFuzzyEquals(-2.5, EquationEvaluator.evaluate("-2.5"));
    }

    @Test
    void testBasicOperations() {
        assertFuzzyEquals(2, EquationEvaluator.evaluate("1+1"));
        assertFuzzyEquals(1, EquationEvaluator.evaluate("3 - 2"));
        assertFuzzyEquals(6, EquationEvaluator.evaluate("2*3"));
        assertFuzzyEquals(2, EquationEvaluator.evaluate("6 / 3"));
        assertFuzzyEquals(-2, EquationEvaluator.evaluate("2-4"));

        assertFuzzyEquals(32, EquationEvaluator.evaluate("64 + -4 * 8"));
        assertFuzzyEquals(8, EquationEvaluator.evaluate("16 + -4 + -4"));
    }

    @Test
    void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> EquationEvaluator.evaluate("hello"));
        assertThrows(IllegalArgumentException.class, () -> EquationEvaluator.evaluate("1 / ("));
        assertThrows(IllegalArgumentException.class, () -> EquationEvaluator.evaluate("1+-*/"));
        assertThrows(IllegalArgumentException.class, () -> EquationEvaluator.evaluate("----1"));
    }

    @Test
    void testWhitespace() {
        assertFuzzyEquals(2, EquationEvaluator.evaluate("    1 +          1"));
        assertFuzzyEquals(1, EquationEvaluator.evaluate("      3 - 2    "));
    }

    @Test
    void testParentheses() {
        assertFuzzyEquals(42, EquationEvaluator.evaluate("10 + 8*(2 + 2)"));
        assertFuzzyEquals(20, EquationEvaluator.evaluate("5 * ((1 + 1) * 2)"));
    }

    @Test
    void testOrderOfOperations() {
        assertFuzzyEquals(1, EquationEvaluator.evaluate("1 - 2 + 3 * 4 / 6"));
        assertFuzzyEquals(4.0 / 3, EquationEvaluator.evaluate("(((1 - 2) + 3) * 4) / 6"));
    }

    @Test
    void testDivideByZero() {
        assertFuzzyEquals(Double.POSITIVE_INFINITY, EquationEvaluator.evaluate("1 / 0"));
        assertFuzzyEquals(Double.POSITIVE_INFINITY, EquationEvaluator.evaluate("1 / (1 - 1)"));
    }

    @Test
    void testImplicitMultiply() {
        assertFuzzyEquals(-2, EquationEvaluator.evaluate("2(3 + 6) - 5(6 - 2)"));
        assertFuzzyEquals(1, EquationEvaluator.evaluate("6 / (2(4 - 1))"));
        assertFuzzyEquals(6 * 5 * 4 * 3 * 2, EquationEvaluator.evaluate("6(5(4(3(2(1)))))"));
    }
}
