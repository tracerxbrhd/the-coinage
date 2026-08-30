package io.github.tracerxbrhd.thecoinage.api.currency;

/** Immutable integer-only denomination ratios used by every Coinage subsystem. */
public record CurrencyRules(long copperPerSilver, long silverPerGold) {
    public static final CurrencyRules DEFAULT = new CurrencyRules(100, 100);

    public CurrencyRules {
        if (copperPerSilver < 2) throw new IllegalArgumentException("copperPerSilver must be at least 2");
        if (silverPerGold < 2) throw new IllegalArgumentException("silverPerGold must be at least 2");
        Math.multiplyExact(copperPerSilver, silverPerGold);
    }

    public long normalizedValue(Denomination denomination) {
        return switch (denomination) {
            case COPPER -> 1L;
            case SILVER -> copperPerSilver;
            case GOLD -> Math.multiplyExact(copperPerSilver, silverPerGold);
        };
    }
}
