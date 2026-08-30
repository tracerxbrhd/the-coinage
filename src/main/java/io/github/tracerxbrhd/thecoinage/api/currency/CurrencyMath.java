package io.github.tracerxbrhd.thecoinage.api.currency;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.OptionalLong;

/** Authoritative overflow-safe conversion, normalization and formatting operations. */
public final class CurrencyMath {
    private CurrencyMath() {}

    public static CurrencyBreakdown normalize(long normalizedCopperValue, CurrencyRules rules) {
        if (normalizedCopperValue < 0) throw new IllegalArgumentException("value cannot be negative");
        long goldValue = rules.normalizedValue(Denomination.GOLD);
        long gold = normalizedCopperValue / goldValue;
        long afterGold = normalizedCopperValue % goldValue;
        long silverValue = rules.normalizedValue(Denomination.SILVER);
        long silver = afterGold / silverValue;
        long copper = afterGold % silverValue;
        return new CurrencyBreakdown(copper, silver, gold);
    }

    public static CurrencyBreakdown normalize(CurrencyBreakdown amount, CurrencyRules rules) {
        return normalize(amount.normalizedValue(rules), rules);
    }

    public static OptionalLong convertExact(CurrencyAmount amount, Denomination target, CurrencyRules rules) {
        long normalized = amount.normalizedValue(rules);
        long targetValue = rules.normalizedValue(target);
        if (normalized % targetValue != 0) return OptionalLong.empty();
        return OptionalLong.of(normalized / targetValue);
    }

    public static String formatNormalized(long value) {
        if (value < 0) throw new IllegalArgumentException("value cannot be negative");
        return NumberFormat.getIntegerInstance(Locale.US).format(value);
    }
}
