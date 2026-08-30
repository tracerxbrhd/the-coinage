package io.github.tracerxbrhd.thecoinage.api.currency;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CurrencyMathTest {
    @Test void defaultValuesAreCentralized() {
        assertEquals(1, CurrencyRules.DEFAULT.normalizedValue(Denomination.COPPER));
        assertEquals(100, CurrencyRules.DEFAULT.normalizedValue(Denomination.SILVER));
        assertEquals(10_000, CurrencyRules.DEFAULT.normalizedValue(Denomination.GOLD));
    }

    @Test void normalizationUsesConfiguredRatios() {
        CurrencyRules rules = new CurrencyRules(10, 20);
        assertEquals(new CurrencyBreakdown(4, 3, 2), CurrencyMath.normalize(434, rules));
        assertEquals(434, new CurrencyBreakdown(4, 3, 2).normalizedValue(rules));
    }

    @Test void conversionCanBeExactOrImpossible() {
        assertEquals(2, CurrencyMath.convertExact(new CurrencyAmount(Denomination.SILVER, 2),
            Denomination.SILVER, CurrencyRules.DEFAULT).orElseThrow());
        assertTrue(CurrencyMath.convertExact(new CurrencyAmount(Denomination.COPPER, 35),
            Denomination.SILVER, CurrencyRules.DEFAULT).isEmpty());
    }

    @Test void zeroAndLargeValuesRemainExact() {
        assertEquals(CurrencyBreakdown.ZERO, CurrencyMath.normalize(0, CurrencyRules.DEFAULT));
        long safe = Long.MAX_VALUE - CurrencyRules.DEFAULT.normalizedValue(Denomination.GOLD);
        assertEquals(safe, CurrencyMath.normalize(safe, CurrencyRules.DEFAULT)
            .normalizedValue(CurrencyRules.DEFAULT));
    }

    @Test void invalidRatiosAndNegativeAmountsFail() {
        assertThrows(IllegalArgumentException.class, () -> new CurrencyRules(1, 100));
        assertThrows(ArithmeticException.class, () -> new CurrencyRules(Long.MAX_VALUE, 2));
        assertThrows(IllegalArgumentException.class, () -> new CurrencyAmount(Denomination.COPPER, -1));
        assertThrows(IllegalArgumentException.class, () -> new CurrencyBreakdown(-1, 0, 0));
    }

    @Test void overflowIsNeverSilentlyWrapped() {
        assertThrows(ArithmeticException.class, () ->
            new CurrencyBreakdown(0, 0, Long.MAX_VALUE).normalizedValue(CurrencyRules.DEFAULT));
        assertThrows(ArithmeticException.class, () ->
            new CurrencyBreakdown(Long.MAX_VALUE, 0, 0).plus(new CurrencyBreakdown(1, 0, 0)));
    }
}
