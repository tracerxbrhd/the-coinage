package io.github.tracerxbrhd.thecoinage.api.currency;

import com.mojang.serialization.Codec;
import java.util.Locale;
import java.util.Optional;

/** Stable identifiers for the three physical Coinage denominations. */
public enum Denomination {
    COPPER("copper"),
    SILVER("silver"),
    GOLD("gold");

    public static final Codec<Denomination> CODEC = Codec.STRING.comapFlatMap(
        value -> byName(value).map(com.mojang.serialization.DataResult::success)
            .orElseGet(() -> com.mojang.serialization.DataResult.error(() -> "Unknown denomination: " + value)),
        Denomination::serializedName);

    private final String serializedName;

    Denomination(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public String translationKey() {
        return "denomination.the_coinage." + serializedName;
    }

    public static Optional<Denomination> byName(String value) {
        if (value == null) return Optional.empty();
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (Denomination denomination : values()) {
            if (denomination.serializedName.equals(normalized)) return Optional.of(denomination);
        }
        return Optional.empty();
    }
}
