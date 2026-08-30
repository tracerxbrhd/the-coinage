package io.github.tracerxbrhd.thecoinage.api.currency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** A non-negative amount in one physical denomination. */
public record CurrencyAmount(Denomination denomination, long count) {
    public static final Codec<CurrencyAmount> CODEC = RecordCodecBuilder.<CurrencyAmount>create(instance -> instance.group(
        Denomination.CODEC.fieldOf("denomination").forGetter(amount -> amount.denomination()),
        Codec.LONG.fieldOf("count").forGetter(amount -> amount.count())
    ).apply(instance, CurrencyAmount::new)).validate(CurrencyAmount::validate);

    public CurrencyAmount {
        if (denomination == null) throw new IllegalArgumentException("denomination cannot be null");
        if (count < 0) throw new IllegalArgumentException("count cannot be negative");
    }

    private static com.mojang.serialization.DataResult<CurrencyAmount> validate(CurrencyAmount amount) {
        return amount.count >= 0
            ? com.mojang.serialization.DataResult.success(amount)
            : com.mojang.serialization.DataResult.error(() -> "Currency count cannot be negative");
    }

    public long normalizedValue(CurrencyRules rules) {
        return Math.multiplyExact(count, rules.normalizedValue(denomination));
    }
}
