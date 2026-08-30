package io.github.tracerxbrhd.thecoinage.purse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Immutable persisted purse payload. Values are deliberately decoded leniently so malformed data
 * remains visible and recoverable instead of being silently replaced with an empty purse.
 */
public record PurseContents(int formatVersion, long copper, long silver, long gold) {
    public static final int CURRENT_FORMAT = 1;
    public static final PurseContents EMPTY = new PurseContents(CURRENT_FORMAT, 0, 0, 0);
    public static final Codec<PurseContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("format_version", CURRENT_FORMAT).forGetter(PurseContents::formatVersion),
        Codec.LONG.optionalFieldOf("copper", 0L).forGetter(PurseContents::copper),
        Codec.LONG.optionalFieldOf("silver", 0L).forGetter(PurseContents::silver),
        Codec.LONG.optionalFieldOf("gold", 0L).forGetter(PurseContents::gold)
    ).apply(instance, PurseContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PurseContents> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, PurseContents::formatVersion,
        ByteBufCodecs.VAR_LONG, PurseContents::copper,
        ByteBufCodecs.VAR_LONG, PurseContents::silver,
        ByteBufCodecs.VAR_LONG, PurseContents::gold,
        PurseContents::new);

    public PurseValidation validate(long capacity) {
        if (formatVersion <= 0 || formatVersion > CURRENT_FORMAT) return PurseValidation.UNSUPPORTED_FORMAT;
        if (copper < 0 || silver < 0 || gold < 0) return PurseValidation.NEGATIVE_COUNT;
        if (copper > capacity || silver > capacity || gold > capacity) return PurseValidation.ABOVE_CAPACITY;
        return PurseValidation.VALID;
    }

    public long get(Denomination denomination) {
        return switch (denomination) {
            case COPPER -> copper;
            case SILVER -> silver;
            case GOLD -> gold;
        };
    }

    public PurseContents with(Denomination denomination, long value) {
        return switch (denomination) {
            case COPPER -> new PurseContents(formatVersion, value, silver, gold);
            case SILVER -> new PurseContents(formatVersion, copper, value, gold);
            case GOLD -> new PurseContents(formatVersion, copper, silver, value);
        };
    }

    public CurrencyBreakdown breakdown() {
        return new CurrencyBreakdown(copper, silver, gold);
    }

    public enum PurseValidation {
        VALID,
        NEGATIVE_COUNT,
        ABOVE_CAPACITY,
        UNSUPPORTED_FORMAT
    }
}
