package io.github.tracerxbrhd.thecoinage.api.currency;

import java.util.List;

/** The four reversible denomination exchanges offered by a Wandering Trader. */
public final class CurrencyExchangeTable {
    private CurrencyExchangeTable() {}

    public static List<Exchange> forRules(CurrencyRules rules) {
        return List.of(
            new Exchange(new CurrencyAmount(Denomination.COPPER, rules.copperPerSilver()),
                new CurrencyAmount(Denomination.SILVER, 1), false),
            new Exchange(new CurrencyAmount(Denomination.SILVER, 1),
                new CurrencyAmount(Denomination.COPPER, rules.copperPerSilver()), false),
            new Exchange(new CurrencyAmount(Denomination.SILVER, rules.silverPerGold()),
                new CurrencyAmount(Denomination.GOLD, 1), false),
            new Exchange(new CurrencyAmount(Denomination.GOLD, 1),
                new CurrencyAmount(Denomination.SILVER, rules.silverPerGold()), true)
        );
    }

    public record Exchange(CurrencyAmount cost, CurrencyAmount reward, boolean rare) {}
}
