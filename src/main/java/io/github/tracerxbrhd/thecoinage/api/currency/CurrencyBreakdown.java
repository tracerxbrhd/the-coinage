package io.github.tracerxbrhd.thecoinage.api.currency;

import java.util.Objects;

/** Immutable counts of every denomination. */
public record CurrencyBreakdown(long copper, long silver, long gold) {
    public static final CurrencyBreakdown ZERO = new CurrencyBreakdown(0, 0, 0);

    public CurrencyBreakdown {
        if (copper < 0 || silver < 0 || gold < 0) {
            throw new IllegalArgumentException("Currency counts cannot be negative");
        }
    }

    public long get(Denomination denomination) {
        return switch (Objects.requireNonNull(denomination, "denomination")) {
            case COPPER -> copper;
            case SILVER -> silver;
            case GOLD -> gold;
        };
    }

    public CurrencyBreakdown with(Denomination denomination, long value) {
        if (value < 0) throw new IllegalArgumentException("Currency count cannot be negative");
        return switch (denomination) {
            case COPPER -> new CurrencyBreakdown(value, silver, gold);
            case SILVER -> new CurrencyBreakdown(copper, value, gold);
            case GOLD -> new CurrencyBreakdown(copper, silver, value);
        };
    }

    public CurrencyBreakdown plus(CurrencyBreakdown other) {
        return new CurrencyBreakdown(
            Math.addExact(copper, other.copper),
            Math.addExact(silver, other.silver),
            Math.addExact(gold, other.gold));
    }

    public boolean isZero() {
        return copper == 0 && silver == 0 && gold == 0;
    }

    public long normalizedValue(CurrencyRules rules) {
        long silverValue = Math.multiplyExact(silver, rules.normalizedValue(Denomination.SILVER));
        long goldValue = Math.multiplyExact(gold, rules.normalizedValue(Denomination.GOLD));
        return Math.addExact(copper, Math.addExact(silverValue, goldValue));
    }
}
